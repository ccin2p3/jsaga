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

import javax.xml.namespace.QName;

/**
 * Defines a basic interface through which resources are discovered, and 
 * removed. The purpose of <code>ResourceHome</code> is to interact
 * with a collection of resources of the same type. Each resource type will
 * have its own custom implementation the <code>ResourceHome</code> interface.
 * The implementation is expected to provide custom methods for creating
 * new resources and optionally methods that act on a set of resource objects.
 */
public interface ResourceHome {

    /**
     * The resource key type. The <code>ResourceKey</code> used or 
     * passed to this <code>ResourceHome</code> must have match this type
     * (corresponds to {@link ResourceKey#getValue() ResourceKey.getValue()}).
     *
     * @return the type of the key.
     */
    Class getKeyTypeClass();
    
    /**
     * The name of the resource key. The <code>ResourceKey</code> used or 
     * passed to this <code>ResourceHome</code> must have match this name 
     * (corresponds to {@link ResourceKey#getName() ResourceKey.getName()}).
     *
     * @return the name of the key.
     */
    QName getKeyTypeName();
    
    /**
     * Retrives a resource.
     * <b>Note:</b> This function must not return null. It must return the 
     * resource object or throw an exception if there is no resource with the
     * specified key.
     *
     * @throws NoSuchResourceException if no resource exists with the given key
     * @throws InvalidResourceKeyException if the resource key is invalid.
     * @throws ResourceException if any other error occurs.
     * @return non-null resource object.
     */
    Resource find(ResourceKey key) 
        throws ResourceException,
               NoSuchResourceException,
               InvalidResourceKeyException;
    
    /**
     * Removes a resource.
     * If the resource implements the {@link RemoveCallback RemoveCallback}
     * interface, the implementation must invoke this the remove operation
     * on the resource.
     *
     * @throws NoSuchResourceException if no resource exists with the given key
     * @throws InvalidResourceKeyException if the resource key is invalid.
     * @throws RemoveNotSupportedException if remove operation is not 
     *         supported.
     * @throws ResourceException if any other error occurs.
     */
    void remove(ResourceKey key) 
        throws ResourceException, 
               NoSuchResourceException,
               InvalidResourceKeyException,
               RemoveNotSupportedException;
    
}
