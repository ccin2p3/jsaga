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
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.util.I18n;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * Handles elements that are used to specify credentials in the
 * security descriptor, namely &lt;proxy-file&gt; and &lt;credential&gt;
 */
public class CredentialParamsParser implements ElementHandler {

    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);

    public static final String PROXY_FILE_NAME = "proxy-file";
    public static final String CREDENTIAL_NAME = "credential";
    public static final String KEY_FILE_NAME = "key-file";
    public static final String CERT_FILE_NAME = "cert-file";
    public static final String ATTRIB_NAME = "value";

    public static final QName PROXY_FILE_QNAME =
        new QName(SecurityDescriptor.NS, PROXY_FILE_NAME);
    public static final QName CREDENTIAL_QNAME =
        new QName(SecurityDescriptor.NS, CREDENTIAL_NAME);

    protected CredentialParamsParserCallback callback;

    public CredentialParamsParser(CredentialParamsParserCallback callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {

        String name = elem.getLocalName();
        if (name.equalsIgnoreCase(PROXY_FILE_NAME)) {
            callback.setProxyFilename(elem.getAttribute(ATTRIB_NAME));
        } else if (name.equalsIgnoreCase(CREDENTIAL_NAME)) {

            String certFile = null;
            String keyFile = null;
            int i=0;
            for (Node childElem = elem.getFirstChild();
                 childElem != null;
                 childElem = childElem.getNextSibling()) {

                     if (childElem.getNodeType() != Node.ELEMENT_NODE) {
                         continue;
                     }

                     if (childElem.getLocalName().equalsIgnoreCase(
                         CERT_FILE_NAME)) {
                         certFile = ((Element)childElem).getAttribute(ATTRIB_NAME);
                         i++;
                     } else if (childElem.getLocalName().equalsIgnoreCase(
                         KEY_FILE_NAME)) {
                         keyFile = ((Element)childElem).getAttribute(ATTRIB_NAME);
                         i++;
                     }

                     if (i == 2)
                         break;
                 }
            if (i != 2)
                throw new SecurityDescriptorException(
                    i18n.getMessage("badCredElem"));
            callback.setCertificateFiles(certFile, keyFile);
        }
    }
}
