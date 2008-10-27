package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that the object on which a method is called is in a
 * state where that method cannot succeed.
 */
public class IncorrectStateException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an IncorrectState exception.
     */
    public IncorrectStateException() {
        super(INCORRECT_STATE);
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public IncorrectStateException(String message) {
        super(INCORRECT_STATE, message);
    }

    /**
     * Constructs an IncorrectState exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public IncorrectStateException(Throwable cause) {
        super(INCORRECT_STATE, cause);
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public IncorrectStateException(String message, Throwable cause) {
        super(INCORRECT_STATE, message, cause);
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message
     * and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public IncorrectStateException(String message, SagaObject object) {
        super(INCORRECT_STATE, message, object);
    }
    
    /**
     * Constructs a new IncorrectStateException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public IncorrectStateException(Throwable cause, SagaObject object) {
        super(INCORRECT_STATE, cause, object);
    }

    /**
     * Constructs a new IncorrectStateException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public IncorrectStateException(String detail, Throwable cause, SagaObject object) {
        super(INCORRECT_STATE, detail, cause, object);
    }

}
