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
package org.globus.wsrf.impl.security.descriptor.util;

import org.globus.util.I18n;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;

public class ElementParser implements ElementHandler {

    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.descriptor.util.errors",
                     ElementParser.class.getClassLoader());
    protected transient Map elementHandlers;
    private QName elemName;

    protected ElementParser() {}

    public ElementParser(QName elemName) {
        elementHandlers = new HashMap();
        this.elemName = elemName;
    }

    public void register(QName name, ElementHandler handler) {
        elementHandlers.put(name, handler);
    }

    public void parse(Element configElement) throws ElementParserException {
        checkElement(configElement, elemName);

        for (
            Node currentChild = configElement.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element elem = (Element) currentChild;
            QName name = getQName(elem);
            ElementHandler handler = (ElementHandler) elementHandlers.get(name);

            if (handler == null) {
                throw new ElementParserException(
                    i18n.getMessage("noHandler", name)
                );
            }

            handler.parse(elem);
        }
    }

    public static QName getQName(Node node) {
        return new QName(node.getNamespaceURI(), node.getLocalName());
    }

    public static void checkElement(Element configElement, QName expectedName)
        throws ElementParserException {

        if (expectedName != null) {
            QName cfgName = getQName(configElement);

            if (!cfgName.equals(expectedName)) {
                throw new ElementParserException(
                    i18n.getMessage(
                        "invalidElement", new Object[] { cfgName,
                                                         expectedName }));
            }
        }
    }

    public static Node getFirstChildElement(Node elem) {
        for (
            Node currentChild = elem.getFirstChild(); currentChild != null;
                currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) currentChild;
            }
        }

        return null;
    }
}
