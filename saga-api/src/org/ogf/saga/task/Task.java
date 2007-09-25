package org.ogf.saga.task;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectSession;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.ReadOnly;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.monitoring.Monitorable;

/**
 * Tasks can only be created through asynchronous method calls.
 */
public interface Task extends SagaBase, Monitorable {

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
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Waits for the task to end up in a final state.
     * The SAGA API specifies that this method is called <code>wait</code>,
     * for Java the name needs to be changed slightly.
     * @param timeoutInSeconds maximum number of seconds to wait.
     * @return wether the task is finished within the specified time.
     */
    public boolean waitTask(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Cancels the asynchronous operation.
     * This is a non-blocking version, which may continue to try and cancel
     * the task in the background. The task state will remain RUNNING until the
     * cancel operation succeeds.
     */
    public void cancel()
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Cancels the asynchronous operation.
     * If it does not succeed to cancel the task within the specified timeout,
     * it may continue to try and cancel the task in the background.
     * The task state will remain RUNNING until the cancel operation succeeds.
     * @param timeoutInSeconds maximum time for freeing resources.
     */
    public void cancel(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Get the state of the task.
     * @return the state of the task.
     */
    public State getState()
        throws NotImplemented, Timeout, NoSuccess;

    /**
     * Rethrow any exception a failed task caught.
     */
    public void rethrow()
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist, ReadOnly,
            Timeout, NoSuccess;
}
