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
package org.globus.wsrf.tools.wsdl;

import javax.xml.namespace.QName;

public interface WSDLPreprocessorConstants
{
    public static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public static final String WSDLPP_NS =
        "http://www.globus.org/namespaces/2004/10/WSDLPreprocessor";
    public static final String WSRP_NS =
        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd";
    public static final QName EXTENDS = new QName(WSDLPP_NS,
                                                  "extends");
    public static final QName RP = new QName(WSRP_NS,
                                             "ResourceProperties");

    public static final String WSA_NS =
        "http://schemas.xmlsoap.org/ws/2004/03/addressing";

    public static final QName WSA_ACTION =
        new QName(WSA_NS, "Action");
}
