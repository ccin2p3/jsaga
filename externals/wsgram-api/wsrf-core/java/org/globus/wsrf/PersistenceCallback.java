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
 * Defines callback operations for persistence operations. These operations
 * will be invoked when used with
 * {@link org.globus.wsrf.impl.ResourceHomeImpl ResourceHomeImpl}.
 * Currently, the <code>ResourceHomeImpl</code> will only invoke the
 * {@link #load(ResourceKey) load()} function automatically.
 * The resource implementation itself is responsible for calling
 * {@link #store() store()} function to synchronize its state on disk.
 */
public interface PersistenceCallback {

    /**
     * Loads the resource state.
     *
     * @throws NoSuchResourceException if no resource state exists for the
     *         specified key.
     * @throws InvalidResourceKeyException if the resource key is invalid.
     * @throws ResourceException if the load operation fails for any other
     *         reason.
     */
    void load(ResourceKey key)
        throws ResourceException,
               NoSuchResourceException,
               InvalidResourceKeyException;

    /**
     * Saves the resource state.
     *
     * @throws ResourceException if the store operation fails.
     */
    void store()
        throws ResourceException;

}
