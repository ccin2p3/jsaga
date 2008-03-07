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
package org.globus.wsrf.security.authorization;

import javax.xml.namespace.QName;

public class PDPConstants {

    public static final String RESOURCE = 
        "org.globus.wsrf.impl.security.authorization.errors";


    public static final String XACML_PDP = "XACMLPDP";

    // Custom action used to check pdp authorization
    public static final String ACTION = "org.globus.security.pdp.action";

    // Trusted targets that may be set by clients
    public static final String TRUSTED_TARGETS = 
        "org.globus.security.target.trusted";

    // FIXME
    // SAM namespace
    public static final String SERVICE_AUTHORIZATION_MANAGEMENT_NS =
	"http://www.globus.org/namespaces/2004/04/security/authorization/sam";

    // Trusted Target Handler Error
    public static final QName TARGET_NOT_ALLOWED_ERROR =
	new QName(SERVICE_AUTHORIZATION_MANAGEMENT_NS, "TrustedTargetError");

    public static final String SERVICE_POLICIES_TAG = "sam:servicePolicies";
}
