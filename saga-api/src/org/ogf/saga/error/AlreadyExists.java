package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicate that an operation cannot succeed because the
 * entity to be created or registered already exists or is already registered.
 */
public class AlreadyExists extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AlreadyExists exception.
     */
    public AlreadyExists() {
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message.
     * @param message the detail message.
     */
    public AlreadyExists(String message) {
        super(message);
    }

    /**
     * Constructs an AlreadyExists exception with the specified cause.
     * @param cause the cause.
     */
    public AlreadyExists(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message
     * and cause. 
     * @param message the detail message.
     * @param cause the cause.
     */
    public AlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an AlreadyExists exception with the specified detail message
     * and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public AlreadyExists(String message, SagaBase object) {
        super(message, object);
    }
}
