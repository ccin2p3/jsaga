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
        super(NOT_IMPLEMENTED);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public NotImplemented(String message) {
        super(NOT_IMPLEMENTED, message);
    }

    /**
     * Constructs a NotImplemented exception with the specified cause.
     * @param cause the cause.
     */
    public NotImplemented(Throwable cause) {
        super(NOT_IMPLEMENTED, cause);
    }

    /**
     * Constructs a NotImplemented exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public NotImplemented(String message, Throwable cause) {
        super(NOT_IMPLEMENTED, message, cause);
    }
    
    /**
     * Constructs a NotImplemented exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public NotImplemented(String message, SagaObject object) {
        super(NOT_IMPLEMENTED, message, object);
    }
}
