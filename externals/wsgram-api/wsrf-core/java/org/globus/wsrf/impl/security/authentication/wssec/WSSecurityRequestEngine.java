/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.impl.security.authentication.wssec;

import java.security.PrivateKey;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.proxy.ProxyPathValidator;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.descriptor.SecurityPropertiesHelper;
import org.globus.wsrf.providers.GSSPublicKey;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.ContextUtils;

public class WSSecurityRequestEngine extends WSSecurityEngine {

    private static Log log =
        LogFactory.getLog(WSSecurityRequestEngine.class.getName());

    private static WSSecurityEngine engine;

    public synchronized static WSSecurityEngine getEngine() {
        if (engine == null) {
            engine = new WSSecurityRequestEngine();
        }

        return engine;
    }

    public Document processSecurityHeader(SOAPEnvelope env,
                                          MessageContext msgCtx)
        throws Exception {
        return processSecurityHeader(env, msgCtx, true);
    }

    public boolean verifyXMLSignature(XMLSignature sig,
                                      MessageContext msgCtx)
        throws Exception {


        ProxyPathValidator validator = new ProxyPathValidator();

        // get resource
        Resource resource = null;
        try {
            ResourceContext context = ResourceContext
                .getResourceContext((org.apache.axis.MessageContext)msgCtx);
            resource = context.getResource();
        } catch (ResourceContextException exp) {
            //TODO: quiet catch
            log.debug("Resource does not exist ", exp);
            resource = null;
        } catch (ResourceException exp) {
            //TODO: quiet catch
            log.debug("Resource does not exist ", exp);
            resource = null;
        }

        String servicePath = ContextUtils
            .getTargetServicePath((org.apache.axis.MessageContext)msgCtx);

        Boolean rejectLimState =
            SecurityPropertiesHelper.getRejectLimitedProxyState(servicePath,
                                                                resource);
        if (Boolean.TRUE.equals(rejectLimState)) {
            log.debug("Reject Limited Proxy is true, service");
            validator.setRejectLimitedProxyCheck(true);
        }
        return verifyXMLSignature(sig, msgCtx, validator);
    }

    public boolean decryptXMLEncryption(Element element,
                                        MessageContext msgCtx)
        throws Exception {

        log.debug("Enter: decryptXMLEncryption");

        // Ensure it was signed also
        ensureSignature(msgCtx);

        PrivateKey key = null;
        // Get credential from container or service to get private key
        SecurityManager manager =
            SecurityManager.getManager((org.apache.axis.MessageContext)msgCtx);
        Subject subject = manager.getSystemSubject();
        if (subject != null) {
            Set privateCreds = subject.getPrivateCredentials();
            if (privateCreds != null) {
                Iterator iterator = privateCreds.iterator();
                if (iterator.hasNext()) {
                    GlobusGSSCredentialImpl cred =
                        (GlobusGSSCredentialImpl)iterator.next();
                    key = cred.getPrivateKey();
                }
            }
        }

        if (key == null) {
            log.error("No credentials to decrypt");
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "noCreds");
        }
        msgCtx.setProperty(Constants.GSI_SEC_MSG, Constants.ENCRYPTION);
        return decryptXMLEncryption(element, key);
    }

    public boolean verifyGssXMLSignature(XMLSignature sig,
                                         MessageContext msgContext)
        throws Exception {

        log.debug("Enter: verifyGssXMLSignature");

        KeyInfo keyInfo = sig.getKeyInfo();
        boolean result = false;

        SecurityContext context =
            GSSSecurityEngine.getContext(
                (org.apache.axis.MessageContext)msgContext, keyInfo);

        if (context == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "noContext01");
        }

        result = sig.checkSignatureValue(new GSSPublicKey(
            (String) context.getID(), context.getContext()));

        // set some other properties here
        setContextProperties(msgContext, context, Constants.SIGNATURE);

        log.debug("Exit: verifyGssXMLSignature");

        return result;
    }


}
