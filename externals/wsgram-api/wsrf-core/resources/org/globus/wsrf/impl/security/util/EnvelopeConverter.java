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

import org.w3c.dom.Document;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;


public abstract class EnvelopeConverter {

    private static EnvelopeConverter converter;

    static {
        converter = new AxisEnvelopeConverter();
    }

    public static EnvelopeConverter getInstance() {
        return converter;
    }

    public abstract Document toDocument(SOAPEnvelope env)
        throws Exception;

    public abstract SOAPMessage toSOAPMessage(Document doc)
        throws Exception;

    public abstract SOAPEnvelope toSOAPEnvelope(Document doc)
        throws Exception;
}
