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

import javax.crypto.SecretKey;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;

import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.WSSecurityEngine;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.conversation.message.token.SecurityContextToken;
import org.apache.ws.patched.security.message.token.Reference;
import org.apache.ws.patched.security.message.token.SecurityTokenReference;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.MessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.authentication.secureconv.service.AuthenticationServiceConstants;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.providers.GSSPrivateKey;

public class GSSSecurityEngine extends WSSecurityEngine
{

    private static GSSSecurityEngine engine = new GSSSecurityEngine();

    private static Log log =
        LogFactory.getLog(GSSSecurityEngine.class.getName());

    public static WSSecurityEngine getInstance()
    {
        return engine;
    }

    protected SecretKey getSharedKey(
        Element keyInfoElem, String algorithm, CallbackHandler cb)
        throws WSSecurityException
    {
        KeyInfo keyInfo = null;
        MessageContext msgContext =
            ((WSSecurityCallbackHandler) cb).getContext();
        try
        {
            keyInfo = new KeyInfo(keyInfoElem, WSConstants.SIG_NS);
        }
        catch(XMLSecurityException e)
        {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY,
                "unsupportedKeyInfo", null, e);
        }

        SecurityContext context = null;

        try
        {
            context = getContext(msgContext, keyInfo);
        }
        catch(WSSecurityException e)
        {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY,
                "unsupportedKeyInfo", null, e);
        }

        return new GSSPrivateKey((String) context.getID(),
                                 context.getContext());
    }

    public static SecurityContext getContext(
        org.apache.axis.MessageContext msgContext,
        KeyInfo keyInfo) throws WSSecurityException
    {
        SecurityContext securityContext = null;

        //First try message context:

        securityContext = (SecurityContext)
            msgContext.getProperty(
                org.globus.wsrf.impl.security.authentication.Constants.CONTEXT);

        if(securityContext == null)
        {
            if(keyInfo == null)
            {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "nullKeyInfo");
            }
            String contextId = null;
            WSSConfig wssConfig = WSSConfig.getDefaultWSConfig();

            Node node =
                WSSecurityUtil.getDirectChild(keyInfo.getElement(),
                                              SecurityTokenReference.SECURITY_TOKEN_REFERENCE,
                                              wssConfig.getWsseNS());

            if(node == null)
            {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "unsupportedKeyInfo");
            }

            SecurityTokenReference secRef = null;

            try
            {
                secRef = new SecurityTokenReference(wssConfig, (Element) node);
            }
            catch(WSSecurityException e)
            {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "secTokenReferenceErr", null, e);
            }

            if(secRef.containsReference())
            {
                Document signatureDocument = keyInfo.getDocument();
                Reference ref = null;
                try
                {
                    ref = secRef.getReference();
                }
                catch(WSSecurityException e)
                {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "noReference", null, e);
                }

                if(ref == null)
                {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "noReference");
                }
                Element sctElement =
                    org.apache.ws.patched.security.util.WSSecurityUtil.getElementByWsuId(
                        wssConfig, signatureDocument, ref.getURI());
                if(sctElement == null)
                {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "noContextToken");
                }
                SecurityContextToken secContextToken = null;
                try
                {
                    secContextToken = new SecurityContextToken(
                        sctElement);
                }
                catch(WSSecurityException e)
                {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "invalidContextToken", null, e);
                }
                contextId = secContextToken.getIdentifier();
                if(contextId == null)
                {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "invalidContextToken");
                }
            }
            else
            {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "secTokenReferenceErr");
            }
            securityContext = getContext(msgContext, contextId);
        }
        return securityContext;
    }

    private static SecurityContext
        getContext(org.apache.axis.MessageContext msgContext,
                   String contextId) throws WSSecurityException
    {
        SecurityContext securityContext = null;
        ContainerConfig config =
            ContainerConfig.getConfig(msgContext.getAxisEngine());

        String authServicePath =
            config.getOption(AuthenticationServiceConstants.AUTH_SERVICE);

        log.debug("Authentication service path is " + authServicePath);
        try
        {
            Context initialContext = new InitialContext();
            ResourceHome home =
                (ResourceHome) initialContext.lookup(
                    Constants.JNDI_SERVICES_BASE_NAME
                    + authServicePath + Constants.HOME_NAME);
            log.debug("Context id is " + contextId);
            SimpleResourceKey key =
                new SimpleResourceKey(home.getKeyTypeName(), contextId);
            securityContext = (SecurityContext) home.find(key);
        }
        catch(NamingException e)
        {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY,
                "contextErr", null, e);
        }
        catch(ResourceException e)
        {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY,
                "contextErr", null, e);
        }
        msgContext.setProperty(
            org.globus.wsrf.impl.security.authentication.Constants.CONTEXT,
            securityContext);
        return securityContext;
    }
}
