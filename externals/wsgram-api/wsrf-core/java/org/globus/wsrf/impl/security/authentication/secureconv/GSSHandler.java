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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import org.globus.wsrf.impl.security.authentication.Constants;

/**
 * GSI Secure Conversation - sign or encrypt depending on
 * value of <code>Constants.GSI_SEC_CONV</code>.
 */
public class GSSHandler extends GenericHandler {

    private static Log log = LogFactory.getLog(GSSHandler.class.getName());
    private SignHandler signHandler;
    private EncryptHandler encHandler;

    public boolean handleRequest(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);
    }

    public boolean handleResponse(MessageContext context) {
        return handleMessage((SOAPMessageContext) context);
    }

    public boolean handleMessage(SOAPMessageContext ctx) {
        log.debug("Enter: secure");

        Integer msgSecType = (Integer) ctx.getProperty(Constants.GSI_SEC_CONV);

        log.debug("Request msgSecType " + msgSecType);

        if (msgSecType == null) {
            log.debug("No msg security type set.");
            return true;
        }

        if (msgSecType.equals(Constants.SIGNATURE)) {
            if (signHandler == null) {
                signHandler = new SignHandler();
            }

            signHandler.handleMessage(ctx);
        } else if (msgSecType.equals(Constants.ENCRYPTION)) {
            if (encHandler == null) {
                encHandler = new EncryptHandler();
            }

            encHandler.handleMessage(ctx);
        } else if (msgSecType.equals(Constants.NONE)) {
            log.debug("No msg security.");
        } else {
            log.warn("Invalid msg security type.");
        }

        log.debug("Exit: secure");

        return true;
    }

    public QName[] getHeaders() {
        return null;
    }
}
