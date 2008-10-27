package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that an operation failed semantically. This is the
 * least specific exception in SAGA.
 */
public class NoSuccessException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NoSuccess exception.
     */
    public NoSuccessException() {
        super(NO_SUCCESS);
    }

    /**
     * Constructs a NoSuccess exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public NoSuccessException(String message) {
        super(NO_SUCCESS, message);
    }

    /**
     * Constructs a NoSuccess exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public NoSuccessException(Throwable cause) {
        super(NO_SUCCESS, cause);
    }

    /**
     * Constructs a NoSuccess exception with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public NoSuccessException(String message, Throwable cause) {
        super(NO_SUCCESS, message, cause);
    }

    /**
     * Constructs a NoSuccess exception with the specified detail message and
     * associated SAGA object.
     * 

     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public NoSuccessException(String message, SagaObject object) {
        super(NO_SUCCESS, message, object);
    }
    /**
     * Constructs a new NoSuccessException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public NoSuccessException(Throwable cause, SagaObject object) {
        super(NO_SUCCESS, cause, object);
    }

    /**
     * Constructs a new NoSuccessException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public NoSuccessException(String detail, Throwable cause, SagaObject object) {
        super(NO_SUCCESS, detail, cause, object);
    }

}
