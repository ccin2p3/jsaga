package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicates that the object on which a method is called is in a
 * state where that method cannot succeed.
 */
public class IncorrectState extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an IncorrectState exception.
     */
    public IncorrectState() {
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message.
     * @param message the detail message.
     */
    public IncorrectState(String message) {
        super(message);
    }

    /**
     * Constructs an IncorrectState exception with the specified cause.
     * @param cause the cause.
     */
    public IncorrectState(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message
     * and cause. 
     * @param message the detail message.
     * @param cause the cause.
     */
    public IncorrectState(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an IncorrectState exception with the specified detail message
     * and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public IncorrectState(String message, SagaBase object) {
        super(message, object);
    }
}
