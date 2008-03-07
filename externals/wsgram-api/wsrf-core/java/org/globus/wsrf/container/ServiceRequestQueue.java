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

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * queue managing multithreaded request enqueuing and dequeueing
 */
public class ServiceRequestQueue {
    LinkedList requests = new LinkedList();
    int waitingThreads = 0;
    static Log logger = LogFactory.getLog(ServiceRequestQueue.class.getName());
    private Semaphore semaphore = new Semaphore();

    public ServiceRequest dequeue() throws InterruptedException {
        addWaitingThread();
        semaphore.waitForSignal();
        removeWaitingThread();

        logger.debug("Getting request from queue");

        Object request = null;

        synchronized (this) {
            request = this.requests.removeFirst();
        }

        return (ServiceRequest) request;
    }

    private synchronized void addWaitingThread() {
        this.waitingThreads++;
    }

    private synchronized void removeWaitingThread() {
        this.waitingThreads--;
    }

    public synchronized int enqueue(ServiceRequest request) {
        logger.debug("Putting request in queue");

        this.requests.addLast(request);
        semaphore.sendSignal();

        return this.waitingThreads;
    }

    public synchronized void stopThreads(int threads) {
        for (int i = 0; i < threads; i++) {
            this.requests.addFirst(null);
            semaphore.sendSignal();
        }
    }
}
