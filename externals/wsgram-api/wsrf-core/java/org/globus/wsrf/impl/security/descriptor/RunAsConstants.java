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

public interface RunAsConstants {
    public static final int CALLER = 1;
    public static final int SYSTEM = 2;
    public static final int SERVICE = 3;
    public static final int RESOURCE = 4;
    // run-as element comparison strings
    public static final String ELEMENT_RUNAS = "run-as";
    public static final String CALLER_ID = "caller-identity";
    public static final String SYSTEM_ID = "system-identity";
    public static final String SERVICE_ID = "service-identity";
    public static final String RESOURCE_ID = "resource-identity";
}
