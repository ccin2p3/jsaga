package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a remote operation did not complete
 * successfully because the network communication or the remote service timed
 * out.
 */
public class TimeoutException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Timeout exception.
     */
    public TimeoutException() {
        super(TIMEOUT);
    }

    /**
     * Constructs a Timeout exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public TimeoutException(String message) {
        super(TIMEOUT, message);
    }

    /**
     * Constructs a Timeout exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public TimeoutException(Throwable cause) {
        super(TIMEOUT, cause);
    }

    /**
     * Constructs a Timeout exception with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public TimeoutException(String message, Throwable cause) {
        super(TIMEOUT, message, cause);
    }

    /**
     * Constructs a Timeout exception with the specified detail message and
     * associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public TimeoutException(String message, SagaObject object) {
        super(TIMEOUT, message, object);
    }

    /**
     * Constructs a new TimeoutException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public TimeoutException(Throwable cause, SagaObject object) {
        super(TIMEOUT, cause, object);
    }

    /**
     * Constructs a new TimeoutException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public TimeoutException(String detail, Throwable cause, SagaObject object) {
        super(TIMEOUT, detail, cause, object);
    }

}
