package org.ogf.saga.task;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.monitoring.Monitorable;

/**
 * Tasks can only be created through asynchronous method calls.
 * The generic parameter <code>E</code> denotes the type of the return
 * value of the asynchronous method call.
 */
public interface Task<E> extends SagaObject, Monitorable {

    /**
     * Metric name: fires on task state change, and has the literal value
     * of the task state enumeration.
     */
    public static final String TASK_STATE = "Task.state";

    /**
     * Starts the asynchronous operation.
     */
    public void run()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Waits for the task end up in a final state.
     * The SAGA API specifies that this method is called <code>wait</code>,
     * for Java the name needs to be changed slightly.
     */
    public void waitTask()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Waits for the task to end up in a final state.
     * The SAGA API specifies that this method is called <code>wait</code>,
     * for Java the name needs to be changed slightly.
     * @param timeoutInSeconds maximum number of seconds to wait.
     * @return <code>true</code> if the task is finished within the specified time.
     */
    public boolean waitTask(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Cancels the asynchronous operation.
     * This is a non-blocking version, which may continue to try and cancel
     * the task in the background. The task state will remain RUNNING until the
     * cancel operation succeeds.
     */
    public void cancel()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Cancels the asynchronous operation.
     * If it does not succeed to cancel the task within the specified timeout,
     * it may continue to try and cancel the task in the background.
     * The task state will remain RUNNING until the cancel operation succeeds.
     * @param timeoutInSeconds maximum time for freeing resources.
     */
    public void cancel(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Gets the state of the task.
     * @return the state of the task.
     */
    public State getState()
        throws NotImplemented, Timeout, NoSuccess;
    
    /**
     * Obtains the result of the asynchronous method call.
     * @return the result.
     * @exception IncorrectState is thrown when the task is not in state DONE.
     */
    public E getResult() throws NotImplemented, IncorrectState, Timeout,
           NoSuccess;

    /**
     * Gets the object from which the task was created.
     * @return the object this task was created from.
     */
    public SagaObject getObject()
        throws NotImplemented, Timeout, NoSuccess;

    /**
     * Throws any exception a failed task caught.
     */
    public void rethrow()
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
}
