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

import commonj.timers.CancelTimerListener;
import commonj.timers.Timer;
import commonj.timers.TimerListener;

public class TimerImpl implements Timer
{
    private TimerListenerWrapper timerTask;
    private TimerListener listener;
    private long period;

    public TimerImpl(TimerListener listener, long period)
    {
        this.period = period;
        this.listener = listener;
    }

    /* (non-Javadoc)
     * @see commonj.timer.Timer#cancel()
     */
    public boolean cancel()
    {
        if(this.listener instanceof CancelTimerListener)
        {
            ((CancelTimerListener) this.listener).timerCancel(this);
        }
        return timerTask.cancel();
    }

    /* (non-Javadoc)
     * @see commonj.timer.Timer#getPeriod()
     */
    public long getPeriod()
    {
        return this.period;
    }

    /* (non-Javadoc)
     * @see commonj.timer.Timer#getTimerListener()
     */
    public TimerListener getTimerListener()
    {
        return this.listener;
    }

    /* (non-Javadoc)
     * @see commonj.timer.Timer#scheduledExecutionTime()
     */
    public long scheduledExecutionTime()
    {
        return this.timerTask.scheduledExecutionTime() + this.period;
    }

    /**
     * @return Returns the timerTask.
     */
    protected TimerListenerWrapper getTimerTask()
    {
        return this.timerTask;
    }

    /**
     * @param timerTask The timerTask to set.
     */
    protected void setTimerTask(TimerListenerWrapper timerTask)
    {
        this.timerTask = timerTask;
    }

}
