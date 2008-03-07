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

import org.globus.wsrf.Resource;

import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

/**
 * Defines the interface that a secure resource should implement. 
 */
public interface SecureResource extends Resource {

    /**
     * Method to retrieve the security descriptor for this
     * resource. If the descriptor does not have Subject and GridMap
     * set, then it is recommended that
     * <code>ResourceSecurityConfig</code> be used initialize the
     * descriptor object. If the <i>initialized</i> is set to true, in
     * the returned descriptor, then no initialization is done. 
     *
     * @return resource security descriptor for the resource. Can be null.
     */
    public ResourceSecurityDescriptor getSecurityDescriptor();
}
