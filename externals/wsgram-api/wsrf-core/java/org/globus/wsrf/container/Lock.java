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

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * This class is a simple reentrant lock implementation.
 */
public class Lock extends ReentrantLock {

    private LockManager lockManager;
    private Object key;
    
    public Lock(LockManager lockManager, Object key) {
        this.lockManager = lockManager;
        this.key = key;
    }

    public synchronized void release() {
        super.release();
        if (this.holds_ == 0 && this.lockManager != null) {
            this.lockManager.removeLock(this.key);
        }
    }
    
}
