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
package org.globus.wsrf.impl.security.authentication.secureconv.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSContext;

import org.globus.wsrf.Constants;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityDescriptor;

import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.TimerManager;

/**
 * Creates and manages <code>SecurityContext</code>
 */
public class SecurityContextHome extends ResourceHomeImpl
    implements AuthenticationServiceConstants {

    static Log logger =
        LogFactory.getLog(SecurityContextHome.class.getName());

    private static final UUIDGen uuidGen =
        UUIDGenFactory.getUUIDGen();

    public synchronized void initialize() throws Exception
    {
        super.initialize();
        logger.debug("Initializing timer");
        ExpiredContextTimerListener listener =
            new ExpiredContextTimerListener(this);
        int interval = ExpiredContextTimerListener.getInterval();
        try {
            Context initialContext = new InitialContext();
            TimerManager timerManager = (TimerManager)
                initialContext.lookup(Constants.DEFAULT_TIMER);
            timerManager.schedule(listener, interval, interval);
        } catch(NamingException e) {
            logger.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public SimpleResourceKey create(GSSContext context) {
        String contextId = uuidGen.nextUUID();
        logger.debug("Context id " + contextId);
        // Context id is unique enough.
        SecurityContext secCtx = new SecurityContext(context, contextId);
        SimpleResourceKey key =
            new SimpleResourceKey(keyTypeName, contextId);
        add(key, secCtx);
        return key;
    }

    public synchronized void removeExpiredContexts() {
        logger.debug("scanning for expired contexts");

        Set entries = this.resources.entrySet();
        SecurityContext context;
        Iterator iterator = entries.iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            context = (SecurityContext) entry.getValue();

            if (context.getContext().isEstablished() &&
                context.getContext().getLifetime() <= 0) {
                    logger.debug("removing expired context");
                    iterator.remove();
                }
        }
    }
}

class ExpiredContextTimerListener implements TimerListener {

    private static final int DEFAULT_INTERVAL = 10 * 60 * 1000;
    private SecurityContextHome home = null;

    public ExpiredContextTimerListener(SecurityContextHome home) {
        this.home = home;
    }

    static int getInterval() {
        try {
            String sweeperInterval = null;
            ContainerSecurityDescriptor desc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (desc != null)
                sweeperInterval = desc.getContextTimerInterval();

            if (sweeperInterval != null) {
                return Integer.parseInt(sweeperInterval);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return DEFAULT_INTERVAL;
    }

    public void timerExpired(Timer timer) {
        if (this.home != null) {
            this.home.removeExpiredContexts();
        }
    }
}
