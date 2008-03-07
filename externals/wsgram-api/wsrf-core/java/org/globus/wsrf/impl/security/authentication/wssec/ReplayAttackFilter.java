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
package org.globus.wsrf.impl.security.authentication.wssec;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityDescriptor;
import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.TimerManager;

public class ReplayAttackFilter {

    protected static I18n i18n = I18n
        .getI18n("org.globus.wsrf.impl.security.authentication.wssec.errors");
    private static Log logger =
        LogFactory.getLog(ReplayAttackFilter.class.getName());

    // This in minutes. Window will be (current - 5) to (current + 5)
    private static int DEFAULT_WINDOW_RANGE = 5;
    private static int WINDOW_RANGE = 5;
    private static ReplayAttackFilter replayFilter = new ReplayAttackFilter();
    private static ExpiredMessageIdTimerListener listener;
    protected Hashtable messageIdTable = new Hashtable();

    protected ReplayAttackFilter() {
    }

    public static ReplayAttackFilter getInstance(String replayWindow) {
        initSweeper();
        if (replayWindow != null) {
            try {
                WINDOW_RANGE = Integer.parseInt(replayWindow);
            } catch (NumberFormatException exp) {
                logger.info("Configured window is erroneous: " + replayWindow
                             + ". using default value of " + WINDOW_RANGE
                             + "minutes");
                WINDOW_RANGE = DEFAULT_WINDOW_RANGE;
            }
        } else
            WINDOW_RANGE = DEFAULT_WINDOW_RANGE;
        return replayFilter;
    }

    private static synchronized void initSweeper() {

        if (listener == null) {
            logger.debug("Initialize sweeper task");
            listener = new ExpiredMessageIdTimerListener(replayFilter);
            int interval = ExpiredMessageIdTimerListener.getInterval();
            try {
                Context initialContext = new InitialContext();
                TimerManager timerManager = (TimerManager)
                    initialContext.lookup(Constants.DEFAULT_TIMER);
                timerManager.schedule(listener, interval, interval);
            } catch(NamingException e) {
                logger.debug("",e);
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private synchronized void put(String messageId, Calendar expirationTime) {
        if ((messageId == null) || (expirationTime == null)) {
            throw new IllegalArgumentException(i18n.getMessage("msgIdNull")
                                               + i18n
                                               .getMessage("expirationNull"));
        }
        this.messageIdTable.put(messageId, expirationTime);
    }

    private Calendar get(String messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException(i18n.getMessage("msgIdNull"));
        }
        return (Calendar) this.messageIdTable.get(messageId);
    }

    private boolean inRange(Calendar base, Calendar val) {

        Calendar lowerBound = Calendar.getInstance();
        lowerBound.setTime(base.getTime());
        lowerBound.add(Calendar.MINUTE, (-1 * WINDOW_RANGE));

        Calendar upperBound = Calendar.getInstance();
        upperBound.setTime(base.getTime());
        upperBound.add(Calendar.MINUTE, WINDOW_RANGE);

        if ((val.after(lowerBound)) && (val.before(upperBound))) {
            return true;
        } else {
            return false;
        }
    }

    public void checkMessageValidity(String messageId, Calendar created)
        throws WSSecurityException {

        logger.debug("Message id is " + messageId);
        // Window.
        Calendar now = Calendar.getInstance();
        logger.debug("Current time is " + now.getTime());
        logger.debug("Created time is " + created.getTime());
        if (inRange(now, created)) {
            logger.debug("message in window");
            Calendar msgExpiration = get(messageId);
            // Message exists in window and is valid, throw error
            if (logger.isDebugEnabled()) {
                if (msgExpiration != null) {
                    logger.debug("Base " + now.getTime());
                    logger.debug("expiration " + msgExpiration.getTime());
                }
            }
            if ((msgExpiration != null) && (inRange(now, msgExpiration))) {
                String err = i18n.getMessage("duplicateMsg", messageId);
                logger.debug(err);
                throw new WSSecurityException(WSSecurityException.FAILURE,
                                              "duplicateMsg");
            } else {
                // either msg does not exist. if it does, it is not
                // valid.
                created.add(Calendar.MINUTE, WINDOW_RANGE);
                logger.debug("Valid message, adding to table: " + messageId +
                             " expiration time " + created.getTime());
                put(messageId, created);
            }
        } else {
            String err = i18n.getMessage("msgExpNotInWin");
            logger.debug(err);
            throw new WSSecurityException(WSSecurityException.MESSAGE_EXPIRED);
        }
    }

    public synchronized void removeExpiredValues() {

        logger.debug("scanning for expired messgae Id");
        Set entries = this.messageIdTable.entrySet();
        Iterator iterator = entries.iterator();

        // FIXME - get one instance of Calendar and reuse ?
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Calendar expirationTime = (Calendar) entry.getValue();
            if (expirationTime.before(Calendar.getInstance())) {
                logger.debug("removing expired message id value");
                iterator.remove();
            }
        }
    }

    public int getMessageWindow() {
        return WINDOW_RANGE;
    }
}

class ExpiredMessageIdTimerListener implements TimerListener {

    private static final int DEFAULT_INTERVAL = 1 * 60 * 1000;
    private ReplayAttackFilter replayFilter = null;

    public ExpiredMessageIdTimerListener(ReplayAttackFilter replayFilter) {
        this.replayFilter = replayFilter;
    }

    static int getInterval() {
        try {
            String sweeperInterval = null;
            ContainerSecurityDescriptor desc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (desc != null) {
                sweeperInterval = desc.getReplayTimerInterval();
            }

            if (sweeperInterval != null) {
                return Integer.parseInt(sweeperInterval);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return DEFAULT_INTERVAL;
    }

    public void timerExpired(Timer timer) {
        if (replayFilter != null) {
            replayFilter.removeExpiredValues();
        }
    }
}
