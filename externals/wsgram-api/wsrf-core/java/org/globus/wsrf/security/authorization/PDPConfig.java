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

import org.globus.wsrf.impl.security.authorization.InterceptorConfig;
import org.globus.wsrf.impl.security.authorization.exceptions.ConfigException;

/**
 * This interface is used to encapsulate and shield the interceptor 
 * configuration mechanism from the core PDP framework.
 * The configuration is associated with an interceptor between the
 * initialize and close calls.
 * @see Interceptor
 * @see PIP
 * @see PDP
 */
public interface PDPConfig {
    /**
     * gets the interceptors class names to be loaded, and their names 
     * (configuration scopes)
     * @return array of interceptor configurations
     */
    public InterceptorConfig[] getInterceptors() throws ConfigException;

    /**
     * gets a property based on the scoped name of the interceptor 
     * @param name scoped name of interceptor
     * @param property name of property to get 
     * @return the property or null if not found
     */ 
    public Object getProperty(String name, String property);

    /**
     * sets a property based on the scoped name of the interceptor
     * @param name scoped name of interceptor
     * @param property name of property to set 
     * @param value value of property to set 
     */ 
    public void setProperty(String name, String property, Object value);
}
