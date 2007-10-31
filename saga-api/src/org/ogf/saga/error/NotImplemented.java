package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a SAGA method is not implemented.
 */
public class NotImplemented extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NotImplemented exception.
     */
    public NotImplemented() {
    }

    /**
     * Constructs a NotImplemented exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public NotImplemented(String message) {
        super(message);
    }

    /**
     * Constructs a NotImplemented exception with the specified cause.
     * @param cause the cause.
     */
    public NotImplemented(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public NotImplemented(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a NotImplemented exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public NotImplemented(String message, SagaObject object) {
        super(message, object);
    }
}
