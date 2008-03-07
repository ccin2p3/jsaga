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

import commonj.work.WorkItem;

public class WorkItemImpl implements WorkItem
{
    private int status = -1;

    /* (non-Javadoc)
     * @see commonj.work.WorkItem#getStatus()
     */
    public synchronized int getStatus()
    {
        return this.status;
    }

    /**
     * @param status The status to set.
     */
    protected synchronized void setStatus(int status)
    {
        // only set status if new status is later in the sequence
        if(status > this.status)
        {
            this.status = status;
        }
    }

}
