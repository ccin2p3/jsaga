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
package org.globus.wsrf.impl.security.authentication.securemsg;

import org.ietf.jgss.GSSCredential;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.wsrf.impl.security.authentication.signature.X509WSSignedSOAPEnvelopeBuilder;

import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;

import org.globus.wsrf.impl.security.util.AuthUtil;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import org.globus.gsi.gssapi.JaasGssUtil;
import org.globus.gsi.gssapi.jaas.JaasSubject;

import org.globus.util.I18n;

/**
 * Used for GSI Secure Message. Signs and adds relevant information
 * into the security header.
 */
public class X509SignHandler extends GenericHandler {

    protected static I18n i18n = 
        I18n.getI18n("org.globus.wsrf.impl.security.authentication.errors");
    private static Log log = 
        LogFactory.getLog(X509SignHandler.class.getName());

    public boolean handleRequest(MessageContext context) {
        return handleMessage((SOAPMessageContext) context, 
                             JaasSubject.getCurrentSubject());
    }

    public boolean handleResponse(MessageContext context) {
        // Since Subject.doAs() is done in the privot handler
        // in our case, this handler is executed after the 
        // service invocation was done. Therefore, the 
        // Subject is no longer associated with the current thread.
        // To use the right Subject for the outgoing message
        // we have to pick up the right subject from the
        // invocation subject property.
        return handleMessage((SOAPMessageContext) context,
                             (Subject)context
                             .getProperty(Constants.INVOCATION_SUBJECT));
    }

    public boolean handleMessage(SOAPMessageContext ctx, Subject subject) {

        Object tmp = ctx.getProperty(Constants.GSI_SEC_MSG);

        if ((!Constants.SIGNATURE.equals(tmp)) &&  
            (!Constants.ENCRYPTION.equals(tmp))) {
            log.debug("Signature not requested. " + tmp);
            return true;
        }
        
        SOAPMessage msg = ctx.getMessage();
        if (msg == null) {
            log.debug("No message - not signing.");
            return true;
        }

        log.debug("Enter: sign");

        SOAPMessage signedMsg = null;

        try {
            SOAPEnvelope unsignedEnvelope = msg.getSOAPPart().getEnvelope();

            // Get credential from message context if set there, if
            // not use the credential associated with the thread.
            GSSCredential cred = AuthUtil.getCredential(ctx);

            if ((cred == null) && (subject != null)) {
                log.debug("Get credentials associated with current thread");
                cred = JaasGssUtil.getCredential(subject);
            }

            X509WSSignedSOAPEnvelopeBuilder builder =
                new X509WSSignedSOAPEnvelopeBuilder(ctx, cred);

            builder.setActor((String) ctx.getProperty("x509Actor"));

            signedMsg = builder.buildMessage(unsignedEnvelope);
        } catch (Exception e) {
            log.error(i18n.getMessage("signErr"), e);
            throw WSSecurityFault.makeFault(e);
        }

        ctx.setMessage(signedMsg);

        log.debug("Exit: sign");

        return true;
    }

    public QName[] getHeaders() {
        return null;
    }
}
