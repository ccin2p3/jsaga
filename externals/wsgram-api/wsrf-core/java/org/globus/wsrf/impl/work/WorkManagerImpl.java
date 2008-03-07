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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;
import commonj.work.WorkManager;
import commonj.work.WorkRejectedException;

public class WorkManagerImpl implements WorkManager
{
    protected static final int MAX_POOL_SIZE = 5;
    private PooledExecutor pool;
    private Map workItems = new HashMap();
    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public WorkManagerImpl()
    {
        this(MAX_POOL_SIZE);
    }

    public WorkManagerImpl(int maxPoolSize)
    {
        this.pool = new PooledExecutor(maxPoolSize);
    }

    public void setMaximumPoolSize(int maxPoolSize)
    {
        this.pool.setMaximumPoolSize(maxPoolSize);
    }

    public int getMaximumPoolSize()
    {
        return this.pool.getMaximumPoolSize();
    }

    /* (non-Javadoc)
     * @see commonj.work.WorkManager#schedule(commonj.work.Work, commonj.work.WorkListener)
     */
    public synchronized WorkItem schedule(Work work, WorkListener listener)
        throws WorkException, IllegalArgumentException
    {
        WorkEvent event;

        if(work == null)
        {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "work"));
        }

        WorkItem workItem = new WorkItemImpl();

        this.workItems.put(work, workItem);

        try
        {
            if(work.isDaemon())
            {
                Thread thread = new Thread(new WorkWrapper(work, listener));
                thread.setDaemon(true);
                thread.start();
            }
            else
            {
                this.pool.execute(new WorkWrapper(work, listener));
            }
        }
        catch(Exception e)
        {
            event = new WorkEventImpl(WorkEvent.WORK_REJECTED, work);
            this.processEvent(event);
            if(listener != null)
            {
                listener.workRejected(event);
            }

            throw new WorkRejectedException(e);
        }

        event = new WorkEventImpl(WorkEvent.WORK_ACCEPTED, work);
        this.processEvent(event);
        if(listener != null)
        {
            listener.workAccepted(event);

        }

        return workItem;
    }

    /* (non-Javadoc)
     * @see commonj.work.WorkManager#schedule(commonj.work.Work)
     */
    public synchronized WorkItem schedule(Work work) throws WorkException,
        IllegalArgumentException
    {
        return this.schedule(work, null);
    }

    /* (non-Javadoc)
     * @see commonj.work.WorkManager#waitForAll(java.util.Collection, long)
     */
    public boolean waitForAll(Collection workItems, long timeout)
    {
        Iterator workItemsIterator = workItems.iterator();
        Object current;
        WorkItem workItem;
        int status;

        if(timeout == WorkManager.IMMEDIATE)
        {
            while(workItemsIterator.hasNext())
            {
                current = workItemsIterator.next();
                if(current instanceof WorkItem)
                {
                    status = ((WorkItem) current).getStatus();
                    if(status != WorkEvent.WORK_COMPLETED &&
                       status != WorkEvent.WORK_REJECTED)
                    {
                        return false;
                    }
                }
            }
        }
        else if(timeout == WorkManager.INDEFINITE)
        {
            while(workItemsIterator.hasNext())
            {
                current = workItemsIterator.next();
                if(current instanceof WorkItem)
                {
                    workItem = (WorkItem) current;
                    synchronized(workItem)
                    {
                        status = workItem.getStatus();
                        while(status != WorkEvent.WORK_COMPLETED &&
                              status != WorkEvent.WORK_REJECTED)
                        {
                            try
                            {
                                workItem.wait();
                            }
                            catch(InterruptedException e)
                            {
                                // not sure if this is the right thing to do
                                return false;
                            }
                            status = workItem.getStatus();
                        }
                    }
                }
            }
        }
        else
        {
            long absTimeout = System.currentTimeMillis() + timeout;
            while(workItemsIterator.hasNext())
            {
                current = workItemsIterator.next();
                if(current instanceof WorkItem)
                {
                    workItem = (WorkItem) current;
                    synchronized(workItem)
                    {
                        status = workItem.getStatus();
                        while(status != WorkEvent.WORK_COMPLETED &&
                              status != WorkEvent.WORK_REJECTED)
                        {
                            if(timeout > 0)
                            {
                                try
                                {
                                    workItem.wait(timeout);
                                }
                                catch(InterruptedException e)
                                {
                                    // not sure if this is the right thing to do
                                    return false;
                                }
                                timeout = absTimeout -
				    System.currentTimeMillis();
                            }
                            else
                            {
                                return false;
                            }

                            status = workItem.getStatus();
                        }
                    }
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
    * @see commonj.work.WorkManager#waitForAny(java.util.Collection, long)
    */
    public Collection waitForAny(Collection workItems, long timeout)
    {
        Iterator workItemsIterator = workItems.iterator();
        Object current;
        WorkItem workItem;
        int status;
        Collection result = new Vector();

        if(timeout == WorkManager.IMMEDIATE)
        {
            while(workItemsIterator.hasNext())
            {
                current = workItemsIterator.next();
                if(current instanceof WorkItem)
                {
                    workItem = (WorkItem) current;
                    status = workItem.getStatus();
                    if(status == WorkEvent.WORK_COMPLETED ||
                       status == WorkEvent.WORK_REJECTED)
                    {
                        result.add(workItem);
                    }
                }
            }
        }
        else if(timeout == WorkManager.INDEFINITE)
        {
            if(this.waitForAll(workItems, timeout))
            {
                return workItems;
            }
            else
            {
                return null;
            }
        }
        else
        {
            long absTimeout = System.currentTimeMillis() + timeout;
            while(workItemsIterator.hasNext())
            {
                current = workItemsIterator.next();
                if(current instanceof WorkItem)
                {
                    workItem = (WorkItem) current;
                    synchronized(workItem)
                    {
                        status = workItem.getStatus();
                        while(status != WorkEvent.WORK_COMPLETED &&
                              status != WorkEvent.WORK_REJECTED &&
                              timeout > 0)
                        {
                            try
                            {
                                workItem.wait(timeout);
                            }
                            catch(InterruptedException e)
                            {
                                // not sure if this is the right thing to do
                                return null;
                            }

                            timeout = absTimeout -
				System.currentTimeMillis();
                            status = workItem.getStatus();
                        }

                        if(status == WorkEvent.WORK_COMPLETED ||
                           status == WorkEvent.WORK_REJECTED)
                        {
                            result.add(workItem);
                        }
                    }
                }
            }
        }

        return result;
    }

    protected synchronized void processEvent(WorkEvent event)
    {
        WorkItemImpl workItem =
                (WorkItemImpl) this.workItems.get(event.getWork());
        if(workItem != null)
        {
            int type = event.getType();
            workItem.setStatus(type);
            if(type == WorkEvent.WORK_COMPLETED ||
               type == WorkEvent.WORK_REJECTED)
            {
                synchronized(workItem)
                {
                    workItem.notifyAll();
                }
                this.workItems.remove(event.getWork());
            }
        }
    }

    protected class WorkWrapper implements Runnable
    {
        private Work work;
        private WorkListener listener;

        public WorkWrapper(Work work, WorkListener listener)
        {
            this.work = work;
            this.listener = listener;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            WorkEvent event =
                    new WorkEventImpl(WorkEvent.WORK_STARTED, this.work);
            processEvent(event);
            if(this.listener != null)
            {
                this.listener.workStarted(event);
            }

            this.work.run();

            event = new WorkEventImpl(WorkEvent.WORK_COMPLETED, this.work);
            processEvent(event);
            if(this.listener != null)
            {
                this.listener.workCompleted(event);
            }
        }
    }
}
