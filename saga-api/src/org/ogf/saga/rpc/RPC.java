package org.ogf.saga.rpc;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * The <code>RPC</code> class represents a remote function handle that can be
 * called repeatedly.
 */
public interface RPC extends SagaObject, Async, Permissions<RPC> {

    /**
     * Calls the remote procedure.
     * 
     * @param parameters
     *      arguments and results for the call.
     * @exception IncorrectURLException
     *      may be thrown here because the RPC server that was
     *      specified to the factory may not have been contacted
     *      before invoking the call.
     * @exception NoSuccessException
     *      is thrown for arbitrary backend failures, with a
     *      descriptive error message.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is thrown when at least one of the parameters of the method
     *      call is ill-formed, invalid, out of bounds or otherwise not
     *      usable, or if the RPC server cannot be found.
     * @exception DoesNotExistException
     *      is thrown when an operation cannot succeed because the RPC server
     *      does not recognize the specified method.
     * @exception IncorrectStateException
     *      is thrown when the object is already closed.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     */
    public void call(Parameter... parameters) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Non-blocking close of the RPC handle instance. Note for Java
     * implementations: A finalizer could be used in case the application
     * forgets to close.
     * @exception NoSuccessException
     *      is thrown for arbitrary backend failures, with a
     *      descriptive error message.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception IncorrectStateException
     *      is in the SAGA specifications, but is not thrown when the object
     *      is already closed.
     */
    public void close() throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Closes the RPC handle instance. Note for Java implementations: A
     * finalizer could be used in case the application forgets to close.
     * 
     * @param timeoutInSeconds
     *      seconds to wait.
     * @exception NoSuccessException
     *      is thrown for arbitrary backend failures, with a
     *      descriptive error message.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception IncorrectStateException
     *      is in the SAGA specifications, but is not thrown when the object
     *      is already closed.
     */
    public void close(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Creates a task for calling the remote procedure.
     * 
     * @param mode
     *            the task mode.
     * @param parameters
     *            arguments and results for the call.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<RPC, Void> call(TaskMode mode, Parameter... parameters)
            throws NotImplementedException;

    /**
     * Creates a task for closing the RPC handle instance.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<RPC, Void> close(TaskMode mode) throws NotImplementedException;

    /**
     * Creates a task for closing the RPC handle instance.
     * 
     * @param mode
     *            the task mode.
     * @param timeoutInSeconds
     *            seconds to wait.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<RPC, Void> close(TaskMode mode, float timeoutInSeconds)
            throws NotImplementedException;
}
