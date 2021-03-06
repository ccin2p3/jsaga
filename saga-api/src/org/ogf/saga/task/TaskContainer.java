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
 * pulled from a container has type <code>Task&lt;?,?&gt;</code>, and its
 * {@link Task#getObject() getObject()} an {@link Task#getResult()}
 * methods return a {@link java.lang.Object}.
 */
public interface TaskContainer extends SagaObject, Monitorable {

    /**
     * Metric name: fires on state changes of any task in the container, and has
     * the value of that task's object identifier.
     */
    public static final String TASKCONTAINER_STATE = "task_container.state";
    
    /**
     * Adds a task to the task container. If the container already contains the
     * task, the method returns silently.
     * 
     * @param task
     *      the task to add.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void add(Task<?,?> task) throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Removes the specified task from this container.
     * 
     * @param task
     *      the task to be removed.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain the specified task.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void remove(Task<?,?> task) throws NotImplementedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Starts all asynchronous operations in the container.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when any of the tasks in the container
     *      is not in NEW state. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
     */
    public void run() throws NotImplementedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Waits for all tasks to end up in a final state. This
     * method blocks indefinitely. One of the finished tasks is returned, and
     * removed from the task container.
     * 
     * @return
     *      any of the finished tasks.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      may be thrown when any of the tasks in the container
     *      would throw this on {@link Task#waitFor()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
     */
    public Task<?,?> waitFor() throws NotImplementedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;


    /**
     * Waits for one or more of the tasks to end up in a final state. This
     * method blocks indefinitely. One of the finished tasks is returned, and
     * removed from the task container.
     * 
     * @param mode
     *      wait for ALL or ANY task.
     * @return
     *      any of the finished tasks.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      may be thrown when any of the tasks in the container
     *      would throw this on {@link Task#waitFor()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
     */
    public Task<?,?> waitFor(WaitMode mode) throws NotImplementedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;
    
    /**
     * Waits for all tasks to end up in a final state. One of the
     * finished tasks is returned, and removed from the task container. If none
     * of the tasks is finished within the specified timeout, <code>null</code>
     * is returned.
     * 
     * @param timeoutInSeconds
     *      number of seconds to wait.
     * @return
     *      any of the finished tasks.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      may be thrown when any of the tasks in the container
     *      would throw this on {@link Task#waitFor()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
     */
    public Task<?,?> waitFor(float timeoutInSeconds)
            throws NotImplementedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;
    
    /**
     * Waits for one or more of the tasks to end up in a final state. One of the
     * finished tasks is returned, and removed from the task container. If none
     * of the tasks is finished within the specified timeout, <code>null</code>
     * is returned.
     * 
     * @param timeoutInSeconds
     *      number of seconds to wait.
     * @param mode
     *      wait for ALL or ANY task.
     * @return
     *      any of the finished tasks.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      may be thrown when any of the tasks in the container
     *      would throw this on {@link Task#waitFor()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
     */
    public Task<?,?> waitFor(float timeoutInSeconds, WaitMode mode)
            throws NotImplementedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Cancels all the asynchronous operations in the container. This is a
     * non-blocking version, which may continue to try and cancel tasks in the
     * background. The task states will remain RUNNING until the cancel
     * operation succeeds.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when any of the tasks in the container
     *      would throw this on {@link Task#cancel()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
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
     *      time for freeing resources.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when any of the tasks in the container
     *      would throw this on {@link Task#cancel()}. 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain any tasks.
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
     * Gets a single task from the task container.
     * 
     * @param id
     *      the object identifier of the task (typically obtained from the
     *      <code>TASKCONTAINER_STATE</code> metric).
     * @return
     *      the task.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown when the container does not contain a task for the
     *      specified id.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public Task<?,?> getTask(String id) throws NotImplementedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Gets the tasks in this task container.
     * 
     * @return
     *      the tasks.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public Task<?,?>[] getTasks() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Gets the states of all tasks in the task container.
     * 
     * @return
     *      the states.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public State[] getStates() throws NotImplementedException,
            TimeoutException, NoSuccessException;
}
