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

import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

/**
 * Represents a resouce's security descriptor. 
 */
public class ResourceSecurityDescriptor extends ServiceSecurityDescriptor {

    boolean initialized = false;
    ServiceAuthorizationChain authzChain = null;

    public ResourceSecurityDescriptor() {
        super();
    }

    /**
     * Sets property to indicate of descriptor has been
     * initialized. If true, then credentials and gridmap are assumed
     * to be loaded. To force initialization by framework (i.e
     * loading of credentials/gridmap from file), set it to false.
     */
    public void setInitialized(boolean state) {
        this.initialized = state;
    }

    /**
     * Returns of descriptor has been initialized
     */
    public boolean getInitialized() {
        return this.initialized;
    }

    /**
     * Construct a ServiceAuthorizationChain object and sets the
     * instance in the descriptor.
     */
    public void setAuthzChain(String chain, PDPConfig config, String name, 
                              String id) 
    throws InitializeException {
        this.authzChain = new ServiceAuthorizationChain();
        this.authzChain.initialize(config, name, id);
    }

    /**
     * Sets the authorization chain in descriptor
     */
    public void setAuthzChain(ServiceAuthorizationChain chain) {
        this.authzChain = chain;
    }

    /**
     * Returns the configured service authorization chain
     */
    public ServiceAuthorizationChain getAuthzChain() {
        return this.authzChain;
    }
}
