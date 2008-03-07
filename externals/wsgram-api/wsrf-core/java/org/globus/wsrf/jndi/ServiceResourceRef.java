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
package org.globus.wsrf.jndi;

import org.apache.axis.AxisEngine;

public class ServiceResourceRef extends org.apache.naming.ResourceRef {
    
    private AxisEngine engine;
    private String name;

    public ServiceResourceRef(String resourceClass, String description, 
                              String scope, String auth,
                              AxisEngine engine, String serviceName) {
        super(resourceClass, description, scope, auth, null, null);
        this.engine = engine;
        this.name = serviceName;
    }
    
    public AxisEngine getAxisEngine() {
        return this.engine;
    }
    
    public String getServiceName() {
        return this.name;
    }
    
}
