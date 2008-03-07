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
package org.globus.wsrf;

/**
 * Defines a callback operation that is invoked whenever a resource is removed
 * from {@link ResourceHome ResourceHome}. This is an optional callback 
 * and the resource does need to implement this function if it does not wish
 * to be notified when the resource is removed.
 */
public interface RemoveCallback {
    
    /**
     * Notifies that the resource was removed. This function must not be
     * called directly on the resource object. Only 
     * {@link ResourceHome#remove(ResourceKey) ResourceHome.remove()} is 
     * allowed to call that method during the remove operation.
     *
     * @throws ResourceException if the remove operation fails.
     */
    void remove() throws ResourceException;
    
}
