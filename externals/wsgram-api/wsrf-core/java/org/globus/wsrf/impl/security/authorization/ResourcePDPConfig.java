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

import java.util.HashMap;

import org.globus.wsrf.impl.security.util.AuthUtil;

/**
 * A PDPConfig class that can be used by resources to create dynamic
 * ServiceAuthorizationChain that uses a HashMap to store
 * properties. The properties need to be scoped with relevant prefixes.
 */
public class ResourcePDPConfig extends BasePDPConfig {

    HashMap properties;

    public ResourcePDPConfig(String chain) {
	this.chain = AuthUtil.substitutePDPNames(chain);
        this.properties = new HashMap();
    }

    /**
     * Returns value of property identified by <i>name-property</i> 
     *
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     */
    public Object getProperty(String name, String property) {
        return this.properties.get(name+ "-" + property);
    }

    /**
     * Sets the value of property identified by <i>name-property</i>
     * 
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     * @param obj
     *        Value of the property 
     */
    public void setProperty(String name, String property, Object obj) {
        this.properties.put(name+ "-" + property, obj);
    }
}
