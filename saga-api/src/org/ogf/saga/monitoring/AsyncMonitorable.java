package org.ogf.saga.monitoring;

import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * This interface defines the task versions of the <code>Monitorable</code>
 * interface. Needed for streams.
 */
public interface AsyncMonitorable extends Monitorable {

    /**
     * Creates a task that lists all metrics associated with the object.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String[]> listMetrics(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains a metric instance, identified by name.
     * @param mode the task mode.
     * @param name the name of the metric to be returned.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Metric> getMetric(TaskMode mode, String name)
        throws NotImplemented;

    /**
     * Creates a task that adds a callback to the specified metric.
     * @param mode the task mode.
     * @param name identifier the metric to which the callback is to be added.
     * @param cb the callback to be added.
     * @return a handle to be used for removal of the callback.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Integer> addCallback(TaskMode mode, String name, Callback cb)
        throws NotImplemented;

    /**
     * Creates a task that removes the specified callback.
     * @param mode the task mode.
     * @param name identifier the metric from which the callback is to be
     *     removed.
     * @param cookie identifies the callback to be removed.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task removeCallback(TaskMode mode, String name, int cookie)
        throws NotImplemented;
}
