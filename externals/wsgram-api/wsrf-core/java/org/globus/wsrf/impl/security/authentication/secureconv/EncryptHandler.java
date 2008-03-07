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
package org.globus.wsrf.impl.security.authentication.secureconv;

import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.wsrf.impl.security.authentication.encryption.GssEncryptedSOAPEnvelopeBuilder;

import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.globus.util.I18n;
/**
 * GSI Secure Conversation encryption.
 */
public class EncryptHandler extends GenericHandler {

    protected static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.authentication.errors");
    private static Log log = LogFactory.getLog(EncryptHandler.class.getName());

    public boolean handleRequest(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);
    }

    public boolean handleResponse(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);
    }

    public boolean handleMessage(SOAPMessageContext ctx) {

        // get secure context from the msg context
        SecurityContext secContext =
            (SecurityContext) ctx.getProperty(Constants.CONTEXT);

        if (secContext == null) {
            log.debug("No context - not encrypting.");
            return true;
        }

        SOAPMessage msg = ctx.getMessage();
        if (msg == null) {
            log.debug("No message - not encrypting.");
            return true;
        }

        log.debug("Enter: encrypt");

        SOAPMessage encryptedMsg = null;

        try {
            SOAPEnvelope clearEnvelope = msg.getSOAPPart().getEnvelope();

            GssEncryptedSOAPEnvelopeBuilder builder =
                new GssEncryptedSOAPEnvelopeBuilder(ctx, secContext);

            builder.setActor((String) ctx.getProperty("gssActor"));

            encryptedMsg = builder.buildMessage(clearEnvelope);
        } catch (Exception e) {
            log.error(i18n.getMessage("encryptErr"), e);
            throw WSSecurityFault.makeFault(e);
        }

        ctx.setMessage(encryptedMsg);

        log.debug("Exit: encrypt");

        return true;
    }

    public QName[] getHeaders() {
        return null;
    }
}
