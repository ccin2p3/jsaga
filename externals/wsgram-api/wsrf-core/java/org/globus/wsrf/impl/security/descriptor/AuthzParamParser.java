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

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Handles &lt;authz&gt; element of the security descriptor
 */
public class AuthzParamParser implements ElementHandler {

    public static final String AUTHZ_NAME = "authz";

    public static final QName AUTHZ_QNAME =
        new QName(SecurityDescriptor.NS, AUTHZ_NAME);

    protected AuthzParamParserCallback callback;
    
    public static final String ATTRIB = "value";

    public AuthzParamParser(AuthzParamParserCallback callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {

        String name = elem.getLocalName();
        
        if (name.equalsIgnoreCase(AUTHZ_NAME)) {
            callback.setAuthz(elem.getAttribute(ATTRIB));
        } 
    }
}
