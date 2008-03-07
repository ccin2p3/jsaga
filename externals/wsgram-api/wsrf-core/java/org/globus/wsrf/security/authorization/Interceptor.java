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

import java.io.Serializable;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;

/**
 * Generic interface to be implemented by all interceptors (PIPs and PDPs) in 
 * a chain
 */ 
public interface Interceptor extends Serializable {

    /**
     * Initializes the interceptor with configuration information that
     * are valid up until the point when close is called.
     * @param config holding interceptor specific configuration
     * values, that may be obtained using the name paramter
     *
     * @param name the name that should be used to access all the interceptor 
     *             local configuration
     * @param id the id in common for all interceptors in a chain (it is valid 
     *          up until close is called)
     *          if close is not called the interceptor may assume that the id 
     *          still exists after a process restart
     */ 
    public void initialize(PDPConfig config, String name, String id) 
	throws InitializeException;

    /**
     * this method is called by the PDP framework to indicate that the 
     * interceptor now should remove all state that was allocated in the 
     * initialize call
     */
    public void close() throws CloseException;
}
