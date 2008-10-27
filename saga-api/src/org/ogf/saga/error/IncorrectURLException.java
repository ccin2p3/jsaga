package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a method is given an URL argument that could
 * not be handled.
 */
public class IncorrectURLException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an IncorrectURL exception.
     */
    public IncorrectURLException() {
        super(INCORRECT_URL);
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public IncorrectURLException(String message) {
        super(INCORRECT_URL, message);
    }

    /**
     * Constructs an IncorrectURL exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public IncorrectURLException(Throwable cause) {
        super(INCORRECT_URL, cause);
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public IncorrectURLException(String message, Throwable cause) {
        super(INCORRECT_URL, message, cause);
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message
     * and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public IncorrectURLException(String message, SagaObject object) {
        super(INCORRECT_URL, message, object);
    }

    /**
     * Constructs a new IncorrectURLException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public IncorrectURLException(Throwable cause, SagaObject object) {
        super(INCORRECT_URL, cause, object);
    }

    /**
     * Constructs a new IncorrectURLException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public IncorrectURLException(String detail, Throwable cause, SagaObject object) {
        super(INCORRECT_URL, detail, cause, object);
    }

}
