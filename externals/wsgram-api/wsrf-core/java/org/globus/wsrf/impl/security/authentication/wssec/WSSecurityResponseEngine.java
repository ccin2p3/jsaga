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

import javax.security.auth.Subject;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;

import org.apache.xml.security.signature.XMLSignature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSCredential;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.jaas.JaasGssUtil;
import org.globus.gsi.jaas.JaasSubject;
import org.globus.gsi.proxy.ProxyPathValidator;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.providers.GSSPublicKey;

public class WSSecurityResponseEngine extends WSSecurityEngine {

    private static Log log =
        LogFactory.getLog(WSSecurityResponseEngine.class.getName());

    private static WSSecurityEngine engine;

    public synchronized static WSSecurityEngine getEngine() {
        if (engine == null) {
            engine = new WSSecurityResponseEngine();
        }
        return engine;
    }

    public Document processSecurityHeader(SOAPEnvelope env,
                                          MessageContext msgCtx)
        throws Exception {
        return processSecurityHeader(env, msgCtx, false);
    }

    public boolean verifyGssXMLSignature(XMLSignature sig,
                                         MessageContext msgContext)
        throws Exception {

        log.debug("Enter: verifyGssXMLSignature");

        // get secure context from the msg context
        SecurityContext secContext =
            (SecurityContext) msgContext.getProperty(Constants.CONTEXT);

        if (secContext == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "noContext01");
        }

        boolean result =
            sig.checkSignatureValue(new GSSPublicKey(
                (String) secContext.getID(), secContext.getContext()));

        msgContext.setProperty(Constants.GSI_SEC_CONV, Constants.SIGNATURE);
        log.debug("Exit: verifyGssXMLSignature");

        return result;
    }

    public boolean decryptXMLEncryption(Element element,
                                        MessageContext msgCtx)
        throws Exception {

        // Ensure it was signed also
        ensureSignature(msgCtx);

        GSSCredential cred = null;

        Subject subject = JaasSubject.getCurrentSubject();
        if (subject != null) {
            log.debug("Getting credentials from subject");
            cred = JaasGssUtil.getCredential(subject);
        }
        if (cred == null) {
            log.debug("Getting credentials from property");
            cred = AuthUtil.getCredential(msgCtx);
        }

        GlobusCredential credential = null;
        if (cred == null) {
            credential = GlobusCredential.getDefaultCredential();
        } else {
            if (cred instanceof GlobusGSSCredentialImpl) {
                credential =
                    ((GlobusGSSCredentialImpl) cred).getGlobusCredential();
            }
        }

        PrivateKey key = null;
        if (credential != null) {
            key = credential.getPrivateKey();
        }

        if (key == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "noCreds");
        }

        msgCtx.setProperty(Constants.GSI_SEC_MSG, Constants.ENCRYPTION);
        return decryptXMLEncryption(element, key);
    }

    public boolean verifyXMLSignature(XMLSignature sig,
                                       MessageContext msgCtx)
        throws Exception {

        ProxyPathValidator validator = new ProxyPathValidator();
        return verifyXMLSignature(sig, msgCtx, validator);
    }
}
