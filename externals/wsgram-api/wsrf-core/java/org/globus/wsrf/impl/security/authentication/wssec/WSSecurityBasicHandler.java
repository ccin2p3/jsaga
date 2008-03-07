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

import javax.xml.namespace.QName;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.impl.security.util.EnvelopeConverter;

import org.w3c.dom.Document;

public abstract class WSSecurityBasicHandler extends GenericHandler {

    private static Log log =
        LogFactory.getLog(WSSecurityBasicHandler.class.getName());

    private static final QName[] HEADERS =
        new QName[] { WSConstants.WSSE_QNAME };

    public boolean handleMessage(SOAPMessageContext ctx,
                                 WSSecurityEngine engine) {

        SOAPMessage msg = ctx.getMessage();

        try {
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            Document doc = engine.processSecurityHeader(env, ctx);

            if (doc != null) {
                log.debug("Setting new envelope");

                EnvelopeConverter converter = EnvelopeConverter.getInstance();
                SOAPMessage newMsg = converter.toSOAPMessage(doc);
                ctx.setMessage(newMsg);
            }
        } catch (Exception e) {
            log.debug("",e);
            throw WSSecurityFault.makeFault(e);
        }

        return true;
    }

    public QName[] getHeaders() {
        return HEADERS;
    }

}
