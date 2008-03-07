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
package org.globus.axis.description;

import java.util.ListResourceBundle;

/**
 * I18N class.
 */
public class Resources extends ListResourceBundle
{
    public Object[][] getContents()
    {
        return contents;
    }

    static final Object[][] contents =
        {
            {
                "invalidPivotHandler",
                "[CORE] ''providers'' parameter of ''{0}'' service requires ''{1}'' provider"
            },
            {
                "invalidScope",
                "[CORE] ''scope'' parameter of ''{0}'' service must be set to ''{1}'' or ''{2}''"
            },
            {
                "invalidNumberOfParams",
                "[CORE] Having more than one input parameter is not allowed"
            },
            {
                "missingOperation",
                "[CORE] Operation ''{0}'' defined in wsdl but it''s not implemented in the ''{1}'' service."
            },
            {
                "missingFault",
                "[CORE] Fault ''{0}'' defined in wsdl but it''is not thrown by the ''{1}'' operation in the ''{2}'' service."
            },
            {
                "typeDescNoXmlType",
                "[CORE] No xml type defined in TypeDesc of ''{0}'' class"
            },
            {
                "noFaultTypeDesc",
                "[CORE] No TypeDesc for ''{0}'' fault class"
            },
            {
                "noFaultMessage",
                "[CORE] No wsdl:message entry for ''{0}'' fault for ''{1}'' service"
            },
            {
                "invalidFaultPart",
                "[CORE] No wsdl:part entry or invalid number of entries (0 or more than 1) for ''{0}'' fault for ''{1}'' service"
            },
            {
                "missingElementAttribute",
                "[CORE] Missing ''element'' attribute of wsdl:part entry for ''{0}'' fault for ''{1}'' service"
            },
            {
                "missingElementFault",
                "[CORE] Missing ''xsd:element'' definition for ''{0}'' fault for ''{1}'' service"
            },
            {
                "missingTypeFault",
                "[CORE] Missing ''xsd:type'' definition for ''{0}'' fault for ''{1}'' service"
            },
            {
                "noTypeQName",
                "[CORE] No type qname"
            },
            {
                "noPortConfig",
                "[CORE] No {0}Port parameter defined. Unable to determine service url"
            },
            {
                "noWSDLImportLocationError",
                "[CORE] No ''location'' attribute in wsdl import"
            },
            {
                "noWSDLBinding",
                "[CORE] wsdl binding information missing for service ''{0}''"
            },
            {
                "noSOAPAddressError",
                "[CORE] SOAP address not found"
            }
        };
}
