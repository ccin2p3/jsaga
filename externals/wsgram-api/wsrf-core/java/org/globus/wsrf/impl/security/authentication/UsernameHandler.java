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
package org.globus.wsrf.impl.security.authentication;

import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;

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
 * Used for username/password. Adds relevant information into the
 * security header.
 */
public class UsernameHandler extends GenericHandler {

    protected static I18n i18n = 
        I18n.getI18n("org.globus.wsrf.impl.security.authentication.errors");
    private static Log log = 
        LogFactory.getLog(UsernameHandler.class.getName());

    public boolean handleRequest(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);
    }

    public boolean handleResponse(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);

    }

    public boolean handleMessage(SOAPMessageContext ctx) {

        Object tmp = ctx.getProperty(Constants.USERNAME);

        if (tmp == null) {
            log.debug("User name is not set");
            return true;
        }

        SOAPMessage msg = ctx.getMessage();
        if (msg == null) {
            log.debug("No message");
            return true;
        }
        
        log.debug("user name is " + tmp);
        UsernameSOAPEnvelopeBuilder builder = 
            new UsernameSOAPEnvelopeBuilder(ctx, (String)tmp);
        builder.setActor((String) ctx.getProperty("x509Actor"));
        SOAPMessage soapMsg = null;
        try {
            SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
            soapMsg = builder.buildMessage(envelope);
        } catch (Exception exp) {
            log.error(i18n.getMessage("userPassAdd"), exp);
            throw WSSecurityFault.makeFault(exp);
        }

        ctx.setMessage(soapMsg);

        log.debug("Exit username handler");

        return true;
    }

    public QName[] getHeaders() {
        return null;
    }
}
