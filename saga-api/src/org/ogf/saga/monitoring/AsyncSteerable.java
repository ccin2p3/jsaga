package org.ogf.saga.monitoring;

import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * This interface specifies the Async versions of the methods in
 * <code>Steerable</code>. Needed for job.JobSelf.
 */
public interface AsyncSteerable extends Steerable {
    /**
     * Creates a task that adds a metric instance to the application instance.
     * @param mode the task mode.
     * @param metric the metric instance to be added.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> addMetric(TaskMode mode, Metric metric)
        throws NotImplemented;

    /**
     * Creates a task that removes a metric instance.
     * @param mode the task mode.
     * @param name the name of the metric to be removed.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task removeMetric(TaskMode mode, String name)
        throws NotImplemented;

    /**
     * Creates a task that pushes a new metric value to the backend.
     * @param mode the task mode.
     * @param name the name of the metric to be fired.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task fireMetric(TaskMode mode, String name)
        throws NotImplemented;
}
