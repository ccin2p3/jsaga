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
package org.globus.wsrf.impl;

import javax.xml.namespace.QName;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.RemoveNotSupportedException;

/** 
 * A <code>ResourceHome</code> that always returns a single resource object.
 */
public abstract class SingletonResourceHome implements ResourceHome {
    
    /**
     * The resource object that we will return. Starts off null and is
     * populated when the first find() call is made. 
     */
    private Resource singleResource = null;

    public Class getKeyTypeClass() {
        return null;
    }

    public QName getKeyTypeName() {
        return null;
    }

    public Resource find(ResourceKey key) throws ResourceException {
        if (key != null) {
            throw new NoSuchResourceException();
        }
        synchronized(this) {
            if (this.singleResource == null) {
                // if we do not already have a value, call findSingleton()
                // and then cache the result.
                this.singleResource = findSingleton();
                if (this.singleResource == null) {
                    throw new NoSuchResourceException();
                }
            }
        }
        return this.singleResource;
    }

    /** 
     * Always throws as an exception - not permitted to remove any key. 
     */
    public void remove(ResourceKey key) throws ResourceException {
        throw new RemoveNotSupportedException();
    }

    /** 
     * Finds the single resource to be associated with this resource home. 
     * If this operation returns null a <code>NoSuchResourceException</code>
     * will be raised. <br>
     * If this method returns a non-null reference, then the reference will
     * be cached, and findSingleton will not be called again. This allows, 
     * for example, <code>findSingleton</code> to create a new resource
     * object without needing to track whether it has been called previously.
    */
    protected abstract Resource findSingleton()
        throws ResourceException;
    
}
