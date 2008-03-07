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

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceLifetime;

import commonj.timers.Timer;
import commonj.timers.TimerListener;

public class ResourceSweeper implements TimerListener {

    protected static Log logger =
        LogFactory.getLog(ResourceSweeper.class.getName());

    protected Map resources;
    protected ResourceHome home;

    /**
     *
     * @param resources must be synchronized map
     */
    public ResourceSweeper(ResourceHome home, Map resources) {
        this.home = home;
        this.resources = resources;
    }

    public void timerExpired(Timer timer) {
        logger.debug("cleaning expired resources");

        Calendar currentTime = Calendar.getInstance();
        ResourceKey key;
        Object resource;
        LinkedList list = new LinkedList();

        synchronized(this.resources) {
            Iterator keyIterator = this.resources.keySet().iterator();
            while(keyIterator.hasNext()) {
                key = (ResourceKey)keyIterator.next();
                try {
                    resource = getResource(key);
                    if (resource != null && isExpired(resource, currentTime)) {
                        list.add(key);
                    }
                } catch (ResourceException e) {
                    logger.error("", e);
                }
            }
        }

        Iterator iter = list.iterator();
        while(iter.hasNext()) {
            key = (ResourceKey)iter.next();
            try {
                this.home.remove(key);
            } catch (ResourceException e) {
                logger.error("", e);
            }
        }
    }

    protected Resource getResource(ResourceKey key)
        throws ResourceException {
        return (Resource)this.home.find(key);
    }
    
    protected boolean isExpired(Object resource, Calendar currentTime) {
        if (!(resource instanceof ResourceLifetime)) {
            return false;
        }
        return isExpired((ResourceLifetime)resource, currentTime);
    }
    
    public static boolean isExpired(Resource resource) {
        if (!(resource instanceof ResourceLifetime)) {
            return false;
        }
        return isExpired((ResourceLifetime)resource, Calendar.getInstance());
    }
    
    public static boolean isExpired(ResourceLifetime resource,
                                    Calendar currentTime) {
        Calendar terminationTime = resource.getTerminationTime();
        return (terminationTime != null &&
                terminationTime.before(currentTime));
    }
    
}
