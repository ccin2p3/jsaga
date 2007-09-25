package org.ogf.saga.stream;

import java.io.IOException;
import java.util.List;

import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.monitoring.AsyncMonitorable;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * A client stream object.
 */
public interface Stream extends SagaBase, Async, Attributes,
       AsyncMonitorable {

    // inspection methods

    /**
     * Obtains the URL that was used to create the stream.
     * When this stream is the result of a {@link StreamService#serve()}
     * call, <code>null</code> is returned.
     * @return the URL.
     */
    public URI getUrl()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Returns the remote authorization info.
     * The returned context is deep-copied.
     * @return the remote context.
     */
    public Context getContext()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, Timeout, NoSuccess;

    // management methods

    /**
     * Establishes a connection to the target defined during the construction
     * of the stream.
     * ??? The SAGA specification is not clear on what is to be returned
     * here. The specification sais Context, the detailed specs say
     * void. ???
     */
    public void connect()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, BadParameter, Timeout,
               NoSuccess;

    /**
     * Checks if the stream is ready for I/O, or if it has entered the
     * ERROR state. It will only check for the specified activities.
     * This method blocks until one or more of the specified activities
     * apply.
     * @param what the activities to wait for.
     * @return the activities that apply.
     */
    public List<Activity> waitStream(Activity... what)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, NoSuccess;

    /**
     * Checks if the stream is ready for I/O, or if it has entered the
     * ERROR state. It will only check for the specified activities.
     * If the timeout expires, an empty list is returned.
     * @param what the activities to wait for.
     * @param timeoutInSeconds the timout in seconds.
     * @return the activities that apply.
     */
    public List<Activity> waitStream(float timeoutInSeconds, Activity... what)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, IncorrectState, NoSuccess;

    /**
     * Closes an active connection.
     * This method performs a non-blocking close.
     * I/O is no longer possible. The stream is put in state CLOSED.
     */
    public void close()
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Closes an active connection.
     * I/O is no longer possible. The stream is put in state CLOSED.
     * @param timeoutInSeconds the timeout in seconds.
     */
    public void close(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    // I/O methods

    /**
     * Reads a raw buffer from the stream.
     * @param len the maximum number of bytes to be read.
     * @param buffer the buffer to store into.
     * @return the number of bytes read.
     * @exception IOException deviation from the SAGA specs: thrown in case
     *     of an error.
     */
    public int read(int len, Buffer buffer)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, BadParameter, IncorrectState, Timeout,
               NoSuccess, IOException;

    /**
     * Writes a raw buffer to the stream.
     * Note: if the buffer contains less data than the specified len, only
     * the data in the buffer are written.
     * @param len the number of bytes of data in the buffer.
     * @param buffer the data to be sent.
     * @return the number of bytes written.
     * @exception IOException deviation from the SAGA specs: thrown in case
     *     of an error.
     */
    public int write(int len, Buffer buffer)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
               PermissionDenied, BadParameter, IncorrectState, Timeout,
               NoSuccess, IOException;

    //
    // Task versions ...
    //

    // inspection methods

    /**
     * Creates a task that obtains the URL that was used to create the stream.
     * When this stream is the result of a {@link StreamService#serve()}
     * call, the URL will be <code>null</code>.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<URI> getUrl(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the remote authorization info.
     * The returned context is deep-copied.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Context> getContext(TaskMode mode)
        throws NotImplemented;

    // management methods

    /**
     * Returns a task that
     * establishes a connection to the target defined during the construction
     * of the stream.
     * ??? The SAGA specification is not clear on what is to be returned
     * here. The specification sais Context, the detailed specs say
     * void. ???
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task connect(TaskMode mode)
        throws NotImplemented;

    /**
     * Returns a task that
     * checks if the stream is ready for I/O, or if it has entered the
     * ERROR state. It will only check for the specified activities.
     * @param mode the task mode.
     * @param what the activities to wait for.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<List<Activity>> waitStream(TaskMode mode, Activity... what)
        throws NotImplemented;

    /**
     * Returns a task that
     * checks if the stream is ready for I/O, or if it has entered the
     * ERROR state. It will only check for the specified activities.
     * If the timeout expires, the task will return an empty list.
     * @param mode the task mode.
     * @param what the activities to wait for.
     * @param timeoutInSeconds the timout in seconds.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<List<Activity>> waitStream(TaskMode mode,
            float timeoutInSeconds, Activity... what)
        throws NotImplemented;

    /**
     * Returns a task that closes an active connection.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode)
        throws NotImplemented;

    /**
     * Returns a task that closes an active connection.
     * @param mode the task mode.
     * @param timeoutInSeconds the timeout in seconds.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode, float timeoutInSeconds)
        throws NotImplemented;

    // I/O methods

    /**
     * Creates a task that reads a raw buffer from the stream.
     * @param mode the task mode.
     * @param len the maximum number of bytes to be read.
     * @param buffer the buffer to store into.
     * @return the number of bytes read.
     * @exception IOException deviation from the SAGA specs: thrown in case
     *     of an error.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Integer> read(TaskMode mode, int len, Buffer buffer)
        throws NotImplemented;

    /**
     * Creates a task that writes a raw buffer to the stream.
     * Note: if the buffer contains less data than the specified len, only
     * the data in the buffer are written.
     * @param mode the task mode.
     * @param len the number of bytes of data in the buffer.
     * @param buffer the data to be sent.
     * @return the number of bytes written.
     * @exception IOException deviation from the SAGA specs: thrown in case
     *     of an error.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Integer> write(TaskMode mode, int len, Buffer buffer)
        throws NotImplemented;
}
