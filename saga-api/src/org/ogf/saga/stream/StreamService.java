package org.ogf.saga.stream;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.AsyncMonitorable;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/**
 * A <code>StreamService</code> object establishes a listening/server object
 * that waits for client connections. It is similar to a serversocket.
 */
public interface StreamService extends SagaObject, Async,
        AsyncMonitorable<StreamService>, Permissions<StreamService> {

    // Metrics

    /** Metric name, fires if a client connects. */
    public static final String STREAMSERVER_CLIENTCONNECT = "stream_server.client_connect";

    // Methods

    /**
     * Obtains the URL to be used to connect to this server.
     * 
     * @return 
     *      the URL.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
     * @exception IncorrectStateException
     *      is thrown when the stream service is already closed.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public URL getUrl() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Waits for incoming client connections (like an accept of a serversocket).
     * The returned stream is in OPEN state. This call may block indefinitely.
     * 
     * @return
     *      the client connection.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
     * @exception IncorrectStateException
     *      is thrown when the stream service is already closed.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public Stream serve() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Waits for incoming client connections (like an accept of a serversocket).
     * The returned stream is in OPEN state.
     * 
     * @param timeoutInSeconds
     *      the timeout in seconds.
     * @return
     *      the client connection, or <code>null</code> if the timeout
     *      expires before a client connects.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
     * @exception IncorrectStateException
     *      is thrown when the stream service is already closed.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public Stream serve(float timeoutInSeconds) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Closes a stream service. This is a non-blocking call.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception IncorrectStateException
     *      is thrown when the backend changed its state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void close() throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Closes a stream service.
     * 
     * @param timeoutInSeconds
     *      the timeout in seconds.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception IncorrectStateException
     *      is thrown when the backend changed its state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void close(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Obtains a task to obtain the URL to be used to connect to this server.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<StreamService, URL> getUrl(TaskMode mode)
            throws NotImplementedException;

    /**
     * Obtains a task that waits for incoming client connections (like an accept
     * of a serversocket). The returned stream is in OPEN state.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<StreamService, Stream> serve(TaskMode mode)
            throws NotImplementedException;

    /**
     * Obtains a task that waits for incoming client connections (like an accept
     * of a serversocket). The returned stream is in OPEN state.
     * 
     * @param mode
     *            the task mode.
     * @param timeoutInSeconds
     *            the timeout in seconds.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<StreamService, Stream> serve(TaskMode mode,
            float timeoutInSeconds) throws NotImplementedException;

    /**
     * Obtains a task that closes a stream service.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<StreamService, Void> close(TaskMode mode)
            throws NotImplementedException;

    /**
     * Obtains a task that closes a stream service.
     * 
     * @param mode
     *            the task mode.
     * @param timeoutInSeconds
     *            the timeout in seconds.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<StreamService, Void> close(TaskMode mode, float timeoutInSeconds)
            throws NotImplementedException;
}
