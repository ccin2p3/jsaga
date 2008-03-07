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
package org.globus.wsrf.container;

import java.util.Map;
import java.util.HashMap;

/**
 * Manages a set of locks.
 */
public class LockManager {
    
    private Map locks;
    
    public LockManager() {
        this.locks = new HashMap();
    }
    
    public synchronized Lock getLock(Object key) {
        Lock lock = (Lock)this.locks.get(key);
        if (lock == null) {
            lock = new Lock(this, key);
            this.locks.put(key, lock);
        }
        return lock;
    }
    
    public synchronized Lock removeLock(Object key) {
        return (Lock)this.locks.remove(key);
    }
    
}
