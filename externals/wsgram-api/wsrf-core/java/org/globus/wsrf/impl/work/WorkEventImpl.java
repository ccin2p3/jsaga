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
package org.globus.wsrf.impl.work;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkException;


public class WorkEventImpl implements WorkEvent
{
    private WorkException exception;
    private int type;
    private Work work;   
    
    /**
     * @param type
     * @param work
     */
    public WorkEventImpl(int type, Work work)
    {
        this(null, type, work);
    }
    
    /**
     * @param exception
     * @param type
     * @param work
     */
    public WorkEventImpl(WorkException exception, int type, Work work)
    {
        this.exception = exception;
        this.type = type;
        this.work = work;
    }
    
    /* (non-Javadoc)
     * @see commonj.work.WorkEvent#getException()
     */
    public WorkException getException()
    {
        return this.exception;
    }

    /* (non-Javadoc)
     * @see commonj.work.WorkEvent#getType()
     */
    public int getType()
    {
        return this.type;
    }

    /* (non-Javadoc)
     * @see commonj.work.WorkEvent#getWork()
     */
    public Work getWork()
    {
        return this.work;
    }

}
