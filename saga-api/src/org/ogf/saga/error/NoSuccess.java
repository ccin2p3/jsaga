package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicates that an operation failed semantically.
 * This is the least specific exception in SAGA.
 */
public class NoSuccess extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NoSuccess exception.
     */
    public NoSuccess() {
    }

    /**
     * Constructs a NoSuccess exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public NoSuccess(String message) {
        super(message);
    }

    /**
     * Constructs a NoSuccess exception with the specified cause.
     * @param cause the cause.
     */
    public NoSuccess(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a NoSuccess exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public NoSuccess(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a NoSuccess exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public NoSuccess(String message, SagaBase object) {
        super(message, object);
    }
}
