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

import org.globus.wsrf.impl.security.authorization.Authorization;

public interface ClientParamsParserCallback {

    /**
     * Sets authorization to be set on the client side.
     */
    void setAuthz(Authorization authz);
    
    /**
     * If set, triggers GSI Secure Conversation to be done for the
     * invocation. The protection level is determined by parameter.
     * 
     * @param val
     *        <code>org.globus.wsrf.security.Constants.SIGNATURE</code> or
     *        <code>org.globus.wsrf.security.Constants.ENCRYPTION</code>
     */
    void setGSISecureConv(Integer val);

    /**
     * If set, triggers GSI Secure Message to be done for the
     * invocation. The protection level is determined by parameter.
     * 
     * @param val
     *        <code>org.globus.wsrf.security.Constants.SIGNATURE</code> or
     *        <code>org.globus.wsrf.security.Constants.ENCRYPTION</code>
     */
    void setGSISecureMsg(Integer val);

    /**
     * Sets type of delegation required. 
     */
    void setDelegation(String str);

    /**
     * Sets file location to load peer credentials from
     */
    void setPeerCredentials(String str);

    /**
     * If set, anonymous mode is used
     */
    void setAnonymous();
}
