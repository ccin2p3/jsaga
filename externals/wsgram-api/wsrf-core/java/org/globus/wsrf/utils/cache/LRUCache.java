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
package org.globus.wsrf.utils.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Constants;
import org.globus.wsrf.jndi.Initializable;

import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.TimerManager;

/**
 * LRU cache implementation. The most recently used move to the end of the
 * list while the least recently used stay at the beginning of the list.
 * The least recently used (within a timeout period) are removed periodically.
 */
public class LRUCache
    implements Initializable, TimerListener, Cache {
    
    // default 5 min timeout
    private static final int DEFAULT_TIMEOUT = 1000 * 60 * 5;

    // default 2 min delay
    private static final int DEFAULT_DELAY = 1000 * 60 * 2;
    
    private static Log logger =
        LogFactory.getLog(LRUCache.class.getName());

    private Map cache;
    private LinkedNodeList list;
    private TimerManager timerManager;
    private Timer timer;

    private long timeout = -1;
    private long delay = -1;
    private boolean initialized = false;

    public LRUCache() {
        this.cache = new HashMap();
        this.list = new LinkedNodeList();
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return this.delay;
    }

    public synchronized void initialize() throws Exception {
        if (this.initialized) {
            return;
        }
        if (this.timeout == -1) {
            this.timeout = DEFAULT_TIMEOUT;
        }

        if (this.delay == -1) {
            this.delay = DEFAULT_DELAY;
        }

        Context initialContext = new InitialContext();
        this.timerManager = 
            (TimerManager)initialContext.lookup(Constants.DEFAULT_TIMER);
        
        this.initialized = true;
    }

    private synchronized void resetTimer() {
        this.timer = null;
    }
    
    private synchronized void scheduleTimer() {
        if (this.timer == null) {
            this.timer = this.timerManager.schedule(this, this.delay);
            logger.debug("scheduling timer");
        }
    }

    public void update(Object resource) {
        if (logger.isDebugEnabled()) {
            logger.debug("update: " + resource);
        }
        synchronized(resource) {
            long currTime = System.currentTimeMillis();
            LinkedNodeList.Node node = 
                (LinkedNodeList.Node)this.cache.get(resource);
            if (node == null) {
                Entry entry = new Entry(resource, currTime);
                node = this.list.createNode(entry);
                this.list.add(node);
                this.cache.put(resource, node);
            } else {
                ((Entry)node.getValue()).setAccessTime(currTime);
                this.list.moveToEnd(node); 
            }
        }
        if (this.timer == null) {
            scheduleTimer();
        }
    }
    
    public void remove(Object resource) {
        if (logger.isDebugEnabled()) {
            logger.debug("remove: " + resource);
        }
        synchronized(resource) {
            LinkedNodeList.Node node = 
                (LinkedNodeList.Node)this.cache.get(resource);
            if (node != null) {
                this.list.remove(node);
                this.cache.remove(resource);
                node.setValue(null);
            }
        }
    }
    
    public synchronized void clear() {
        this.cache.clear();
        this.list.clear();
    }

    public int getSize() {
        return this.cache.size();
    }

    public void cleanExpired() {
        logger.debug("cleaning cache");

        Iterator iter = null;
        List expiredResources = null;

        synchronized(this.list) {
            iter = this.list.iterator();
            expiredResources = new LinkedList();
            long currTime = System.currentTimeMillis();
            while(iter.hasNext()) {
                LinkedNodeList.Node n = (LinkedNodeList.Node)iter.next();
                Entry entry = (Entry)n.getValue();
                if (currTime - entry.getAccessTime() > this.timeout) {
                    expiredResources.add(entry.getResource());
                } else {
                    break;
                }
            }
        }

        iter = expiredResources.iterator();
        while(iter.hasNext()) {
            remove(iter.next());
        }

        resetTimer();
        if (!this.list.isEmpty()) {
            scheduleTimer();
        }
    }

    public void timerExpired(Timer timer) {
        cleanExpired();
    }

    private static class Entry {

        private long time;
        private Object resource;

        public Entry(Object resource, long time) {
            this.time = time;
            this.resource = resource;
        }

        public void setResource(Object resource) {
            this.resource = resource;
        }
        
        public Object getResource() {
            return this.resource;
        }

        public void setAccessTime(long time) {
            this.time = time;
        }
        
        public long getAccessTime() {
            return this.time;
        }
    }
    
}

    
