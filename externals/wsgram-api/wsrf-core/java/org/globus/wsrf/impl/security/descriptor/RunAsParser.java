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
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.impl.security.descriptor.util.ElementHandler;
import org.globus.wsrf.impl.security.descriptor.util.ElementParser;
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.util.I18n;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

public class RunAsParser implements ElementHandler, RunAsConstants {
    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);
    public static final QName QNAME =
        new QName(SecurityDescriptor.NS, ELEMENT_RUNAS);
    private static final QName CALLER_QNAME =
        new QName(SecurityDescriptor.NS, CALLER_ID);
    private static final QName SYSTEM_QNAME =
        new QName(SecurityDescriptor.NS, SYSTEM_ID);
    private static final QName SERVICE_QNAME =
        new QName(SecurityDescriptor.NS, SERVICE_ID);
    private static final QName RESOURCE_QNAME =
        new QName(SecurityDescriptor.NS, RESOURCE_ID);
    protected RunAsParserCallback callback;

    public RunAsParser(RunAsParserCallback callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {
        ElementParser.checkElement(elem, QNAME);

        Node child = ElementParser.getFirstChildElement(elem);
        QName name = ElementParser.getQName(child);

        if (name.equals(CALLER_QNAME)) {
            this.callback.setRunAsType(CALLER);
        } else if (name.equals(SYSTEM_QNAME)) {
            this.callback.setRunAsType(SYSTEM);
        } else if (name.equals(SERVICE_QNAME)) {
            this.callback.setRunAsType(SERVICE);
        } else if (name.equals(RESOURCE_QNAME)) {
            this.callback.setRunAsType(RESOURCE);
        } else {
            throw new SecurityDescriptorException(
                i18n.getMessage("badRunAs", name)
            );
        }
    }
}
