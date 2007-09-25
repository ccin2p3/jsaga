package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicates that a method is given an URL argument that could
 * not be handled.
 */
public class IncorrectURL extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an IncorrectURL exception.
     */
    public IncorrectURL() {
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message.
     * @param message the detail message.
     */
    public IncorrectURL(String message) {
        super(message);
    }

    /**
     * Constructs an IncorrectURL exception with the specified cause.
     * @param cause the cause.
     */
    public IncorrectURL(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message
     * and cause. 
     * @param message the detail message.
     * @param cause the cause.
     */
    public IncorrectURL(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an IncorrectURL exception with the specified detail message
     * and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public IncorrectURL(String message, SagaBase object) {
        super(message, object);
    }
}
