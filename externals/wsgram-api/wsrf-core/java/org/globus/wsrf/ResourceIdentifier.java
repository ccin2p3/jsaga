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
 * This interface is used to retrieve a unique id of the resource.
 */
public interface ResourceIdentifier {
    
    /**
     * Returns the unique id of the resource. In most cases this value should
     * match value returned by {@link ResourceKey#getValue 
     * ResoureKey.getValue()}.
     *
     * @return the id of the resource.
     */
    Object getID();
    
}
