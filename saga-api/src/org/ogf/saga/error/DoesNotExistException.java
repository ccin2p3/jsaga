package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that an operation cannot succeed because a required
 * entity is missing.
 */
public class DoesNotExistException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DoesNotExist exception.
     */
    public DoesNotExistException() {
        super(DOES_NOT_EXIST);
    }

    /**
     * Constructs a DoesNotExist exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public DoesNotExistException(String message) {
        super(DOES_NOT_EXIST, message);
    }

    /**
     * Constructs a DoesNotExist exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public DoesNotExistException(Throwable cause) {
        super(DOES_NOT_EXIST, cause);
    }

    /**
     * Constructs a DoesNotExist exception with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public DoesNotExistException(String message, Throwable cause) {
        super(DOES_NOT_EXIST, message, cause);
    }

    /**
     * Constructs a DoesNotExist exception with the specified detail message and
     * associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public DoesNotExistException(String message, SagaObject object) {
        super(DOES_NOT_EXIST, message, object);
    }
    

    /**
     * Constructs a new DoesNotExistException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public DoesNotExistException(Throwable cause, SagaObject object) {
        super(DOES_NOT_EXIST, cause, object);
    }

    /**
     * Constructs a new DoesNotExistException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public DoesNotExistException(String detail, Throwable cause, SagaObject object) {
        super(DOES_NOT_EXIST, detail, cause, object);
    }

}
