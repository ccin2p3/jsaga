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
package org.globus.wsrf.impl.security.authorization;

/**
 * The <code>InterceptorConfig<code> class is used to hold configuration
 * information about an interceptor in a configuration mechanism independent
 * way. It is used by <code>PDPConfig</code>.
 * @see org.globus.wsrf.security.authorization.PDPConfig
 */
public class InterceptorConfig {

    private String interceptorClass;
    private String name;

    /**
     * Constructor
     * @param name the named scope of the interceptor used in configuration
     *             entries
     * @param interceptorClass the class name of the interceptor
     */
    public InterceptorConfig(String name, String interceptorClass) {
        this.interceptorClass = interceptorClass;
        this.name = name;
    }

    /**
     * gets the interceptor class
     * @return the class name of the interceptor
     */
    public String getInterceptorClass() {
        return this.interceptorClass;
    }

    /**
     * gets the interceptor name attached to this interceptor
     * @return the named scope of the interceptor used in configuration
     *             entries
     */
    public String getName() {
        return this.name;
    }
}
