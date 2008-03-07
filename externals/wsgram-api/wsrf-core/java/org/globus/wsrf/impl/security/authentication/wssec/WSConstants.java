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
package org.globus.wsrf.impl.security.authentication.wssec;

import javax.xml.namespace.QName;


public interface WSConstants {
    public static final String WSSE_NS =
        org.apache.ws.patched.security.WSConstants.WSSE_NS;
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSU_NS =
        org.apache.ws.patched.security.WSConstants.WSU_NS;
    public static final String WSU_PREFIX = "wsu";
    public static final String SIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String ENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String SOAP_NS =
        "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String WS_SEC_LN = "Security";
    public static final String WS_SEC_TS_LN = "Timestamp";
    public static final QName WSSE_QNAME = new QName(WSSE_NS, WS_SEC_LN);
}
