package org.ogf.saga.stream;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.SagaIOException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.AsyncMonitorable;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/**
 * A client stream object.
 */
public interface Stream extends SagaObject, Async, AsyncAttributes<Stream>,
        AsyncMonitorable<Stream> {

    // Optional attributes

    /** Attribute name, determines size of send buffer. */
    public static final String BUFSIZE = "Bufsize";

    /**
     * Attribute name, determines the amount of idle time before dropping the
     * connection, in seconds.
     */
    public static final String TIMEOUT = "Timeout";

    /**
     * Attribute name, determines if read/writes are blocking or not. If this
     * attribute is not supported, implementation must do blocking.
     */
    public static final String BLOCKING = "Blocking";

    /**
     * Attribute name, determines if data are compressed before/after transfer.
     */
    public static final String COMPRESSION = "Compression";

    /** Attribute name, determines if packets are sent without delay. */
    public static final String NODELAY = "Nodelay";

    /** Attribute name, determines if all sent data MUST arrive. */
    public static final String RELIABLE = "Reliable";

    // Metrics

    /**
     * Metric name, fires if the state of the stream changes, and has the value
     * of the new state.
     */
    public static final String STREAM_STATE = "stream.state";

    /**
     * Metric name, fires if the stream gets readable (which means that a
     * subsequent read() can successfully read one or more bytes of data).
     */
    public static final String STREAM_READ = "stream.read";

    /**
     * Metric name, fires if the stream gets writable (which means that a
     * subsequent write() can successfully write one or more bytes of data).
     */
    public static final String STREAM_WRITE = "stream.write";

    /** Metric name, fires if the stream has an error condition. */
    public static final String STREAM_EXCEPTION = "stream.exception";

    /** Metric name, fires if the stream gets dropped by the remote party. */
    public static final String STREAM_DROPPED = "stream.dropped";

    // inspection methods

    /**
     * Obtains the URL that was used to create the stream. When this stream is
     * the result of a {@link StreamService#serve()} call, <code>null</code>
     * is returned.
     * 
     * @return the URL.
     */
    public URL getUrl() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Returns the remote authorization info. The returned context is
     * deep-copied.
     * 
     * @return the remote context.
     */
    public Context getContext() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    // management methods

    /**
     * Establishes a connection to the target defined during the construction of
     * the stream.
     */
    public void connect() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Checks if the stream is ready for I/O, or if it has entered the ERROR
     * state. It will only check for the specified activities. This method
     * blocks until one or more of the specified activities apply.
     * 
     * @param what
     *            the activities to wait for.
     * @return the activities that apply.
     */
    public int waitFor(int what) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            NoSuccessException;

    /**
     * Checks if the stream is ready for I/O, or if it has entered the ERROR
     * state. It will only check for the specified activities. If the timeout
     * expires, an empty list is returned.
     * 
     * @param what
     *            the activities to wait for.
     * @param timeoutInSeconds
     *            the timeout in seconds.
     * @return the activities that apply.
     */
    public int waitFor(int what, float timeoutInSeconds)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Closes an active connection. This method performs a non-blocking close.
     * I/O is no longer possible. The stream is put in state CLOSED.
     */
    public void close() throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Closes an active connection. I/O is no longer possible. The stream is put
     * in state CLOSED.
     * 
     * @param timeoutInSeconds
     *            the timeout in seconds.
     */
    public void close(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    // I/O methods

    /**
     * Reads a raw buffer from the stream.
     * 
     * @param len
     *            the maximum number of bytes to be read.
     * @param buffer
     *            the buffer to store into.
     * @return the number of bytes read.
     * @exception SagaIOException
     *                deviation from the SAGA specs: thrown in case of an error,
     *                where the SAGA specs describe a return of a negative
     *                value, corresponding to negatives of the respective ERRNO
     *                error code.
     */
    public int read(Buffer buffer, int len) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Reads a raw buffer from the stream.
     * 
     * @param buffer
     *            the buffer to store into.
     * @return the number of bytes read.
     * @exception SagaIOException
     *                deviation from the SAGA specs: thrown in case of an error,
     *                where the SAGA specs describe a return of a negative
     *                value, corresponding to negatives of the respective ERRNO
     *                error code.
     */
    public int read(Buffer buffer) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Obtains an InputStream from the stream.
     * 
     * @return the inputstream.
     */
    public StreamInputStream getInputStream() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException, SagaIOException;

    /**
     * Writes a raw buffer to the stream. Note: if the buffer contains less data
     * than the specified len, only the data in the buffer are written.
     * 
     * @param len
     *            the number of bytes of data in the buffer.
     * @param buffer
     *            the data to be sent.
     * @return the number of bytes written.
     * @exception SagaIOException
     *                deviation from the SAGA specs: thrown in case of an error,
     *                where the SAGA specs describe a return of a negative
     *                value, corresponding to negatives of the respective ERRNO
     *                error code.
     */
    public int write(Buffer buffer, int len) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Writes a raw buffer to the stream.
     * 
     * @param buffer
     *            the data to be sent.
     * @return the number of bytes written.
     * @exception SagaIOException
     *                deviation from the SAGA specs: thrown in case of an error,
     *                where the SAGA specs describe a return of a negative
     *                value, corresponding to negatives of the respective ERRNO
     *                error code.
     */
    public int write(Buffer buffer) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Obtains an OutputStream from the stream.
     * 
     * @return the outputstream.
     */
    public StreamOutputStream getOutputStream() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException, SagaIOException;

    //
    // Task versions ...
    //

    // inspection methods

    /**
     * Creates a task that obtains the URL that was used to create the stream.
     * When this stream is the result of a {@link StreamService#serve()} call,
     * the URL will be <code>null</code>.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, URL> getUrl(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the remote authorization info. The returned
     * context is deep-copied.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Context> getContext(TaskMode mode)
            throws NotImplementedException;

    // management methods

    /**
     * Returns a task that establishes a connection to the target defined during
     * the construction of the stream.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Void> connect(TaskMode mode)
            throws NotImplementedException;

    /**
     * Returns a task that checks if the stream is ready for I/O, or if it has
     * entered the ERROR state. It will only check for the specified activities.
     * 
     * @param mode
     *            the task mode.
     * @param what
     *            the activities to wait for.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> waitFor(TaskMode mode, int what)
            throws NotImplementedException;

    /**
     * Returns a task that checks if the stream is ready for I/O, or if it has
     * entered the ERROR state. It will only check for the specified activities.
     * If the timeout expires, the task will return an empty list.
     * 
     * @param mode
     *            the task mode.
     * @param what
     *            the activities to wait for.
     * @param timeoutInSeconds
     *            the timout in seconds.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> waitFor(TaskMode mode, int what,
            float timeoutInSeconds) throws NotImplementedException;

    /**
     * Returns a task that closes an active connection.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Void> close(TaskMode mode)
            throws NotImplementedException;

    /**
     * Returns a task that closes an active connection.
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
    public Task<Stream, Void> close(TaskMode mode, float timeoutInSeconds)
            throws NotImplementedException;

    // I/O methods

    /**
     * Creates a task that reads a raw buffer from the stream.
     * 
     * @param mode
     *            the task mode.
     * @param len
     *            the maximum number of bytes to be read.
     * @param buffer
     *            the buffer to store into.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> read(TaskMode mode, Buffer buffer, int len)
            throws NotImplementedException;

    /**
     * Creates a task that reads a raw buffer from the stream.
     * 
     * @param mode
     *            the task mode.
     * @param buffer
     *            the buffer to store into.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> read(TaskMode mode, Buffer buffer)
            throws NotImplementedException;

    /**
     * Creates a task that obtains an OutputStream from the stream.
     * @param mode
     *                the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                    is thrown when the task version of this method is not
     *                    implemented.
     */
    public Task<Stream, StreamInputStream> getInputStream(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that writes a raw buffer to the stream. Note: if the
     * buffer contains less data than the specified len, only the data in the
     * buffer are written.
     * 
     * @param mode
     *            the task mode.
     * @param len
     *            the number of bytes of data in the buffer.
     * @param buffer
     *            the data to be sent.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> write(TaskMode mode, Buffer buffer, int len)
            throws NotImplementedException;

    /**
     * Creates a task that writes a raw buffer to the stream.
     * 
     * @param mode
     *            the task mode.
     * @param buffer
     *            the data to be sent.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Stream, Integer> write(TaskMode mode, Buffer buffer)
            throws NotImplementedException;

    /**
     * Creates a task that obtains an OutputStream from the stream.
     * @param mode
     *                the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                    is thrown when the task version of this method is not
     *                    implemented.
     */
    public Task<Stream, StreamOutputStream> getOutputStream(TaskMode mode)
            throws NotImplementedException;
}
