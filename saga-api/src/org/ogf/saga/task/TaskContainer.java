package org.ogf.saga.task;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.Monitorable;

/**
 * Container object for tasks.
 * Note that a task container must be able to contain any task, so it has
 * no generic parameters for task result type or task object type. The
 * consequence of this is that these types are unknown. So, a task
 * pulled from a container has type <code>Task<?,?></code>, and its
 * {@link Task#getObject() getObject()} an {@link Task#getResult()}
 * methods return a {@link java.lang.Object}.
 */
public interface TaskContainer extends SagaObject, Monitorable {

    /**
     * Metric name: fires on state changes of any task in the container, and has
     * the value of that task's handle.
     */
    public static final String TASKCONTAINER_STATE = "TaskContainer.state";

    /**
     * Adds a task to the task container.
     * 
     * @param task
     *            the task to add.
     * @return a handle allowing for removal of the task.
     */
    public int add(Task<?,?> task) throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Removes the task identified by the specified cookie from this container.
     * 
     * @param cookie
     *            identifies the task.
     * @return the task.
     */
    public Task<?,?> remove(int cookie) throws NotImplementedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Starts all asynchronous operations in the container.
     */
    public void run() throws NotImplementedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Waits for one or more of the tasks to end up in a final state. This
     * method blocks indefinately. One of the finished tasks is returned, and
     * removed from the task container.
     * 
     * @param mode
     *            wait for ALL or ANY task.
     * @return any of the finished tasks.
     */
    public Task<?,?> waitFor(WaitMode mode) throws NotImplementedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Waits for one or more of the tasks to end up in a final state. One of the
     * finished tasks is returned, and removed from the task container. If none
     * of the tasks is finished within the specified timeout, <code>null</code>
     * is returned.
     * 
     * @param timeoutInSeconds
     *            number of seconds to wait.
     * @param mode
     *            wait for ALL or ANY task.
     * @return any of the finished tasks.
     */
    public Task<?,?> waitFor(float timeoutInSeconds, WaitMode mode)
            throws NotImplementedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Cancels all the asynchronous operations in the container. This is a
     * non-blocking version, which may continue to try and cancel tasks in the
     * background. The task states will remain RUNNING until the cancel
     * operation succeeds.
     */
    public void cancel() throws NotImplementedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Cancels all the asynchronous operations in the container. When the
     * timeout expires, this version may continue to try and cancel tasks in the
     * background. The task states will remain RUNNING until the cancel
     * operation succeeds.
     * 
     * @param timeoutInSeconds
     *            time for freeing resources.
     */
    public void cancel(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Returns the number of tasks in this task container.
     * 
     * @return the number of tasks.
     */
    public int size() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Lists the tasks in this task container.
     * 
     * @return an array of cookies.
     */
    public int[] listTasks() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Gets a single task from the task container.
     * 
     * @param cookie
     *            the integer identifying the task.
     * @return the task.
     */
    public Task<?,?> getTask(int cookie) throws NotImplementedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Gets the tasks in this task container.
     * 
     * @return the tasks.
     */
    public Task<?,?>[] getTasks() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Gets the states of all tasks in the task container.
     * 
     * @return the states.
     */
    public State[] getStates() throws NotImplementedException,
            TimeoutException, NoSuccessException;
}
