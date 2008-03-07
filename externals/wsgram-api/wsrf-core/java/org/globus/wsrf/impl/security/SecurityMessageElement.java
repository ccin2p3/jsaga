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
package org.globus.wsrf.impl.security;

import org.w3c.dom.Element;

import org.apache.axis.message.MessageElement;

import org.apache.axis.encoding.SerializationContext;

/**
 * Wrapper class for message element to eliminate some serialization
 * bugs seen on using MessageElement.
 */
public class SecurityMessageElement extends MessageElement {


    Element element = null;

    public SecurityMessageElement(Element elem) {
        super(elem);
        this.element = elem;
    }

    protected void outputImpl(SerializationContext context) throws Exception {
        context.writeDOMElement(this.element);
    }
}
