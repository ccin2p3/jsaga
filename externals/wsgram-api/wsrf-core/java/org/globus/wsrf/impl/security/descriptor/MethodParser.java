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

import org.globus.wsrf.impl.security.descriptor.util.ElementParser;
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.util.I18n;

import org.apache.axis.utils.XMLUtils;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.util.List;

public class MethodParser extends ElementParser implements RunAsParserCallback,
    AuthMethodParserCallback {

    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);
    public static final QName QNAME =
        new QName(SecurityDescriptor.NS, "method");
    protected ServiceSecurityDescriptor descriptor;
    protected QName method;

    public MethodParser(ServiceSecurityDescriptor descriptor) {
        super(QNAME);
        this.descriptor = descriptor;
        register(RunAsParser.QNAME, new RunAsParser(this));
        register(AuthMethodParser.QNAME, new AuthMethodParser(this));
    }

    public void parse(Element element) throws ElementParserException {
        String methodName = element.getAttribute("name");

        if ((methodName == null) || (methodName.length() == 0)) {
            throw new SecurityDescriptorException(
                i18n.getMessage("missingMethod")
            );
        }

        this.method = XMLUtils.getQNameFromString(methodName, element);

        if (this.method == null) {
            throw new SecurityDescriptorException(
                i18n.getMessage("invalidMethod")
            );
        }

        super.parse(element);
    }

    public void setRunAsType(int identity) throws SecurityDescriptorException {
        this.descriptor.setMethodRunAsType(this.method, identity);
    }

    public void setAuthMethods(List methods) throws 
        SecurityDescriptorException {
        this.descriptor.setMethodAuthMethods(this.method, methods);
    }
}
