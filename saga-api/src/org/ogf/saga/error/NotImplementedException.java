package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a SAGA method is not implemented.
 */
public class NotImplementedException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NotImplemented exception.
     */
    public NotImplementedException() {
        super(NOT_IMPLEMENTED);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public NotImplementedException(String message) {
        super(NOT_IMPLEMENTED, message);
    }

    /**
     * Constructs a NotImplemented exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public NotImplementedException(Throwable cause) {
        super(NOT_IMPLEMENTED, cause);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public NotImplementedException(String message, Throwable cause) {
        super(NOT_IMPLEMENTED, message, cause);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail message
     * and associated SAGA object.     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public NotImplementedException(String message, SagaObject object) {
        super(NOT_IMPLEMENTED, message, object);
    }
    
    /**
     * Constructs a new NotImplementedException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public NotImplementedException(Throwable cause, SagaObject object) {
        super(NOT_IMPLEMENTED, cause, object);
    }

    /**
     * Constructs a new NotImplementedException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public NotImplementedException(String detail, Throwable cause, SagaObject object) {
        super(NOT_IMPLEMENTED, detail, cause, object);
    }

}
