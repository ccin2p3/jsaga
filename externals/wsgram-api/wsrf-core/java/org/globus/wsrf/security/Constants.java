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
package org.globus.wsrf.security;

import org.globus.gsi.GSIConstants;

/**
 * Defines public security constants.
 */
public interface Constants extends GSIConstants {

    /** GSI Secure Conversation message protection method type
     * that will be used or was used to protect the request.
     * Can be set to:
     * {@link Constants#SIGNATURE SIGNATURE} or
     * {@link Constants#ENCRYPTION ENCRYPTION} or
     * {@link Constants#NONE NONE}.
     */
    public static final String GSI_SEC_CONV =
        "org.globus.security.secConv.msg.type";

    /** GSI Secure Message protection method type
     * that will be used or was used to protect the request.
     * Can be set to:
     * {@link Constants#SIGNATURE SIGNATURE} or
     * {@link Constants#ENCRYPTION ENCRYPTION} or
     * {@link Constants#NONE NONE}.
     */
    public static final String GSI_SEC_MSG =
        "org.globus.security.secMsg.msg.type";

    /** GSI Secure Converation anonymous flag. If set to
     * <code>Boolean.TRUE</code>, then anonymous authentication is used,
     * or else client credentials are required.
     * @deprecated This option has been superceded by GSI_ANONYMOUS
     */
    public static final String GSI_SEC_CONV_ANON =
        "org.globus.security.secConv.anon";

    /** GSI anonymous flag. If set to
     * <code>Boolean.TRUE</code>, then anonymous authentication is used,
     * if not then client credentials are required.
     */
    public static final String GSI_ANONYMOUS =
        org.globus.axis.gsi.GSIConstants.GSI_ANONYMOUS;
    
    /**
     * Property used to configure client side authorization method. 
     */
    public static final String AUTHORIZATION =
        "org.globus.security.authorization";

    /**
     * Property used to configure client security descritpor filename.
     */
    public static final String CLIENT_DESCRIPTOR_FILE = "clientDescriptorFile";

    /**
     * Property used to configure client security descriptor
     * object. Value of the property should be set to an instance of
     * <code>org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor</code>
     */
    public static final String CLIENT_DESCRIPTOR = "clientDescriptor";

    /**
     * Property used to set username on the message context.
     */
    public static final String USERNAME = "org.globus.security.username";

    /**
     * Property used to set password on the message context.
     */
    public static final String PASSWORD = "org.globus.security.password";

    /**
     * Property used to set password typeon the message context.
     * Should be either <code>WSConstants.PASSWORD_TEXT</code> or
     * <code>WSConstants.PASSWORD_DIGEST</code>
     */
    public static final String PASSWORD_TYPE = 
        "org.globus.security.password.type";
}
