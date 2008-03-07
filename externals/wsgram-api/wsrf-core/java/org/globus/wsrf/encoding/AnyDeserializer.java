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
package org.globus.wsrf.encoding;

import org.apache.axis.MessageContext;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.message.MessageElement;

import org.xml.sax.SAXException;

import java.util.List;

/**
 * Deserializer for MessageElements
 */
public class AnyDeserializer extends DeserializerImpl {

   public static final String DESERIALIZE_CURRENT_ELEMENT = 
       "DeserializeCurrentElement";

    public final void onEndElement(String namespace, 
                                   String localName,
                                   DeserializationContext context)
        throws SAXException {
        try {
            MessageElement msgElem = context.getCurElement();
            if (msgElem != null) {
                MessageContext messageContext = context.getMessageContext();
                Boolean currentElement = (Boolean) messageContext.getProperty(DESERIALIZE_CURRENT_ELEMENT);
                if (currentElement != null && currentElement.booleanValue()) {
                    value = msgElem;
                    messageContext.setProperty(DESERIALIZE_CURRENT_ELEMENT, Boolean.FALSE);
                    return;
                }
                List children = msgElem.getChildren();
                if (children != null) {
                    msgElem = (MessageElement) children.get(0);
                    if (msgElem != null) {
                        value = msgElem;
                    }
                }
            }
        } catch(Exception exp) {
            throw new SAXException(exp);
        }
    }
}
