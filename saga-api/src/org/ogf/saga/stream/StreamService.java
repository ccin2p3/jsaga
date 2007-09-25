package org.ogf.saga.stream;

import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.monitoring.AsyncMonitorable;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * A <code>StreamService</code> object establishes a listening/server object
 * that waits for client connections. It is similar to a serversocket.
 */
public interface StreamService extends SagaBase, Async, AsyncMonitorable,
        Permissions {

    /**
     * Obtains the URL to be used to connect to this server.
     * @return the URL.
     */
    public URI getUrl()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Waits for incoming client connections (like an accept of a
     * serversocket). The returned stream is in OPEN state.
     * This call may block indefinately.
     * @return the client connection.
     */
    public Stream serve()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, BadParameter, NoSuccess;

    /**
     * Waits for incoming client connections (like an accept of a
     * serversocket). The returned stream is in OPEN state.
     * @param timeoutInSeconds the timeout in seconds.
     * @return the client connection, or <code>null</code> if the timeout
     * expires before a client connects.
     */
    public Stream serve(float timeoutInSeconds)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, BadParameter, NoSuccess;

    /**
     * Closes a stream service.
     * This is a non-blocking call.
     */
    public void close()
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Closes a stream service.
     * @param timeoutInSeconds the timeout in seconds.
     */
    public void close(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    //
    // Task versions ...
    //

    /**
     * Obtains a task to obtain the URL to be used to connect to this server.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<URI> getUrl(TaskMode mode)
        throws NotImplemented;

    /**
     * Obtains a task that waits for incoming client connections
     * (like an accept of a serversocket).
     * The returned stream is in OPEN state.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Stream> serve(TaskMode mode)
        throws NotImplemented;

    /**
     * Obtains a task that waits for incoming client connections
     * (like an accept of a serversocket).
     * The returned stream is in OPEN state.
     * @param mode the task mode.
     * @param timeoutInSeconds the timeout in seconds.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Stream> serve(TaskMode mode, float timeoutInSeconds)
        throws NotImplemented;

    /**
     * Obtains a task that closes a stream service.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode)
        throws NotImplemented;

    /**
     * Obtains a task that closes a stream service.
     * @param mode the task mode.
     * @param timeoutInSeconds the timeout in seconds.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode, float timeoutInSeconds)
        throws NotImplemented;
}
