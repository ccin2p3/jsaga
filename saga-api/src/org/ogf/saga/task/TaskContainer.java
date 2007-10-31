package org.ogf.saga.task;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.monitoring.Monitorable;

/**
 * Container object for tasks.
 */
public interface TaskContainer extends SagaObject, Monitorable {

    /**
     * Metric name: fires on state changes of any task in the container,
     * and has the value of that task's handle.
     */
    public static final String TASKCONTAINERSTATE = "TaskContainer.state";

    /**
     * Adds a task to the task container.
     * @param task the task to add.
     * @return a handle allowing for removal of the task.
     */
    public int add(Task task) throws NotImplemented, Timeout, NoSuccess;

    /**
     * Removes the task identified by the specified cookie from this container.
     * @param cookie identifies the task.
     */
    public void remove(int cookie)
        throws NotImplemented, DoesNotExist, Timeout, NoSuccess;

    /**
     * Starts all asynchronous operations in the container.
     */
    public void run()
        throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Waits for one or more of the tasks to end up in a final state.
     * This method blocks indefinately.
     * @param mode wait for ALL or ANY task.
     * @return the finished tasks.
     */
    public Task[] waitTasks(WaitMode mode)
        throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Waits for one or more of the tasks to end up in a final state.
     * @param timeoutInSeconds number of seconds to wait.
     * @param mode wait for ALL or ANY task.
     * @return the finished tasks.
     */
    public Task[] waitTasks(float timeoutInSeconds, WaitMode mode)
        throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Cancels all the asynchronous operations in the container.
     * This is a non-blocking version, which may continue to try and cancel
     * tasks in the background. The task states will remain RUNNING until the
     * cancel operation succeeds.
     */
    public void cancel()
        throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Cancels all the asynchronous operations in the container.
     * When the timeout expires, this version may continue to try and cancel
     * tasks in the background. The task states will remain RUNNING until the
     * cancel operation succeeds.
     * @param timeoutInSeconds time for freeing resources.
     */
    public void cancel(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Returns the number of tasks in this task container.
     * @return the number of tasks.
     */
    public int size()
        throws NotImplemented, Timeout, NoSuccess;

    /**
     * Lists the tasks in this task container.
     * @return an array of cookies.
     */
    public int[] listTasks()
        throws NotImplemented, Timeout, NoSuccess;

    /**
     * Gets a single task from the task container.
     * @param cookie the integer identifying the task.
     * @return the task.
     */
    public Task getTask(int cookie)
        throws NotImplemented, DoesNotExist, Timeout, NoSuccess;

    /**
     * Gets the tasks in this task container.
     * @return the tasks.
     */
    public Task[] getTasks()
        throws NotImplemented, Timeout, NoSuccess;

    /**
     * Gets the states of all tasks in the task container.
     * @return the states.
     */
    public State[] getStates()
        throws NotImplemented, Timeout, NoSuccess;
}
