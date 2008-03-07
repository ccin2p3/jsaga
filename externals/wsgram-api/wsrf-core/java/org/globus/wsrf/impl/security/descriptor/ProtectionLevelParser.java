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

public class ProtectionLevelParser implements ElementHandler {
    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);
    public static final QName QNAME =
        new QName(SecurityDescriptor.NS, "protection-level");
    private boolean privacy = false;
    private boolean integrity = false;

    public ProtectionLevelParser() {
    }

    public void parse(Element elem) throws ElementParserException {
        ElementParser.checkElement(elem, QNAME);

        for (
            Node currentChild = elem.getFirstChild(); currentChild != null;
                currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String name = currentChild.getLocalName();

            if (name.equalsIgnoreCase("none")) {
                throw new SecurityDescriptorException(
                    i18n.getMessage("unsupportedProtLevel", name)
                );
            } else if (name.equalsIgnoreCase("integrity")) {
                this.integrity = true;
            } else if (name.equalsIgnoreCase("privacy")) {
                this.privacy = true;
            } else {
                throw new SecurityDescriptorException(
                    i18n.getMessage("badProtLevel", name)
                );
            }
        }
    }

    public boolean isIntegrity() {
        return this.integrity;
    }

    public boolean isPrivacy() {
        return this.privacy;
    }
}
