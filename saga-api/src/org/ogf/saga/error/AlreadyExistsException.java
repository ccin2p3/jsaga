package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicate that an operation cannot succeed because the entity
 * to be created or registered already exists or is already registered.
 */
public class AlreadyExistsException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AlreadyExists exception.
     */
    public AlreadyExistsException() {
        super(ALREADY_EXISTS);
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public AlreadyExistsException(String message) {
        super(ALREADY_EXISTS, message);
    }

    /**
     * Constructs an AlreadyExists exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public AlreadyExistsException(Throwable cause) {
        super(ALREADY_EXISTS, cause);
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public AlreadyExistsException(String message, Throwable cause) {
        super(ALREADY_EXISTS, message, cause);
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message
     * and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public AlreadyExistsException(String message, SagaObject object) {
        super(ALREADY_EXISTS, message, object);
    }
    
    /**
     * Constructs a new AlreadyExistsException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AlreadyExistsException(Throwable cause, SagaObject object) {
        super(ALREADY_EXISTS, cause, object);
    }

    /**
     * Constructs a new AlreadyExistsException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AlreadyExistsException(String detail, Throwable cause, SagaObject object) {
        super(ALREADY_EXISTS, detail, cause, object);
    }

}
