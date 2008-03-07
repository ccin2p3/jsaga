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
package org.globus.wsrf.impl.timer;

import java.util.TimerTask;

import commonj.timers.Timer;
import commonj.timers.TimerListener;

public class TimerListenerWrapper extends TimerTask
{
    private Timer timer;
    private TimerListener listener;
    private TimerManagerImpl timerManager;
    private boolean suspended = false;
    private boolean expired = false;

    /**
     * @param timer
     * @param timerManager
     */
    public TimerListenerWrapper(Timer timer, TimerManagerImpl timerManager)
    {
        this.timer = timer;
        if(timer instanceof TimerImpl)
        {
            ((TimerImpl) timer).setTimerTask(this);
        }
        this.listener = timer.getTimerListener();
        this.timerManager = timerManager;
    }

    public synchronized void suspend()
    {
        this.suspended = true;
    }

    public synchronized void resume()
    {
        this.suspended = false;
        if(this.expired == true)
        {
            this.listener.timerExpired(timer);
            if(this.timer.getPeriod() == 0)
            {
                this.timerManager.removeTask(this);
            }
            this.expired = false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public synchronized void run()
    {
        if(!this.suspended)
        {
            this.listener.timerExpired(timer);
            if(this.timer.getPeriod() == 0)
            {
                this.timerManager.removeTask(this);
            }
        }
        else
        {
            this.expired = true;
        }
    }

    protected boolean stop()
    {
        return super.cancel();
    }

    public boolean cancel()
    {
        this.timerManager.removeTask(this);
        return super.cancel();
    }

    /**
     * @return Returns the listener.
     */
    protected TimerListener getListener()
    {
        return this.listener;
    }

    /**
     * @return Returns the timer.
     */
    protected Timer getTimer()
    {
        return this.timer;
    }

}
