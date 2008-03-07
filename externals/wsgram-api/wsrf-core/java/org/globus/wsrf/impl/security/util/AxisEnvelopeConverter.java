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
package org.globus.wsrf.impl.security.util;

import org.xml.sax.InputSource;

import org.apache.axis.Message;
import org.apache.axis.encoding.DeserializationContext;

import org.apache.xml.security.c14n.Canonicalizer;

import org.globus.wsrf.config.ContainerConfig;

import org.w3c.dom.Document;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import java.io.ByteArrayInputStream;

public class AxisEnvelopeConverter extends EnvelopeConverter {

    public Document toDocument(SOAPEnvelope env) throws Exception {
        return ((org.apache.axis.message.SOAPEnvelope) env).getAsDocument();
    }

    public SOAPMessage toSOAPMessage(Document doc) throws Exception {
        Canonicalizer c14n = Canonicalizer
            .getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
        byte[] canonicalMessage = c14n.canonicalizeSubtree(doc);
        ByteArrayInputStream in = new ByteArrayInputStream(canonicalMessage);
        MessageFactory factory = MessageFactory.newInstance();

        return factory.createMessage(null, in);
    }

    public SOAPEnvelope toSOAPEnvelope(Document doc) throws Exception {
        return toSOAPEnvelope(doc, ContainerConfig.getContext());
    }

    public SOAPEnvelope toSOAPEnvelope(
        Document doc,
        MessageContext msgContext
    ) throws Exception {
        SOAPEnvelope env = new org.apache.axis.message.SOAPEnvelope();
        toSOAPEnvelope(doc, msgContext, env);

        return env;
    }

    public void toSOAPEnvelope(
        Document doc,
        MessageContext msgContext,
        SOAPEnvelope env
    ) throws Exception {
        Canonicalizer c14n = Canonicalizer
            .getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
        byte[] canonicalMessage = c14n.canonicalizeSubtree(doc);

        InputSource is =
            new InputSource(new ByteArrayInputStream(canonicalMessage));
        DeserializationContext dser = null;
        dser =
            new DeserializationContext(
                is, (org.apache.axis.MessageContext) msgContext, 
                Message.REQUEST,
                (org.apache.axis.message.SOAPEnvelope) env
            );

        dser.parse();
    }
}
