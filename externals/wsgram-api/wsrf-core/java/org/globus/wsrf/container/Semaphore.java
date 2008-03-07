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


/**
 * This class implements a simple semaphore for thread synchronization.
 */
public class Semaphore {

    private int semaphore;
    
    public Semaphore() {
        this(0);
    }
    
    public Semaphore(int initialValue) {
        this.semaphore = initialValue;
    }

    public void acquire() throws InterruptedException {
        waitForSignal();
    }

    public synchronized void waitForSignal() throws InterruptedException {
        if (this.semaphore > 0) {
            this.semaphore--;
            return;
        }
        
        while (semaphore < 1) {
            wait();
        }

        this.semaphore--;
    }

    public void release() {
        sendSignal();
    }
    
    public synchronized void sendSignal() {
        this.semaphore++;
        notify();
    }
    
}
