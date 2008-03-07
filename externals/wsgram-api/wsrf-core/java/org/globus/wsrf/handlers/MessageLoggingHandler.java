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
package org.globus.wsrf.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;

import org.globus.util.I18n;
import org.globus.wsrf.utils.Resources;

/**
 * This handler logs soap messages.
 */
public class MessageLoggingHandler extends BasicHandler {

    private static Log logger =
        LogFactory.getLog(MessageLoggingHandler.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public void invoke(MessageContext messageContext) throws AxisFault {
        logMessage(messageContext);
    }

    public void onFault(MessageContext messageContext) {
        logMessage(messageContext);
    }

    protected void logMessage(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            Message message = messageContext.getCurrentMessage();
            if (message == null) {
                logger.debug("Empty SOAPEnvelope");
            } else {
                try {
                    SOAPEnvelope soapEnvelope = message.getSOAPEnvelope();
                    logger.debug("SOAPEnvelope: " + soapEnvelope);
                } catch (AxisFault af) {
                    logger.error(i18n.getMessage("messageLoggingError"), af);
                }
            }
        }
    }

}
