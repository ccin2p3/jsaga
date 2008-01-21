package org.ogf.saga.rpc;

import org.ogf.saga.SagaObject;
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
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * The <code>RPC</code> class represents a remote function handle
 * that can be called repeatedly.
 */
public interface RPC extends SagaObject, Async, Permissions {

    /**
     * Calls the remote procedure.
     * @param parameters arguments and results for the call.
     * @exception IncorrectURL may be thrown here because the RPC server
     *     that was specified to the factory may not have been contacted
     *     before invoking the call.
     * @exception NoSuccess is thrown for arbitrary backend failures, with
     *     a descriptive error message.
     */
    public void call(Parameter... parameters)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Non-blocking close of the RPC handle instance.
     * Note for Java implementations: A finalizer could be used in case
     * the application forgets to close.
     */
    public void close()
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Closes the RPC handle instance.
     * Note for Java implementations: A finalizer could be used in case
     * the application forgets to close.
     * @param timeoutInSeconds seconds to wait.
     */
    public void close(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    //
    // Task versions ...
    //

    /**
     * Creates a task for calling the remote procedure.
     * @param mode the task mode.
     * @param parameters arguments and results for the call.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task call(TaskMode mode, Parameter... parameters)
        throws NotImplemented;

    /**
     * Creates a task for closing the RPC handle instance.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task for closing the RPC handle instance.
     * @param mode the task mode.
     * @param timeoutInSeconds seconds to wait.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode, float timeoutInSeconds)
        throws NotImplemented;
}
