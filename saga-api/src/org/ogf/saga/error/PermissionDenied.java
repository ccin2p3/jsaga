package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicates that the identity used for the operation
 * did not have sufficient permissions to perform the operation successfully.
 */
public class PermissionDenied extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a PermissionDenied exception.
     */
    public PermissionDenied() {
    }

    /**
     * Constructs a PermissionDenied exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public PermissionDenied(String message) {
        super(message);
    }

    /**
     * Constructs a PermissionDenied exception with the specified cause.
     * @param cause the cause.
     */
    public PermissionDenied(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a PermissionDenied exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public PermissionDenied(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a PermissionDenied exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public PermissionDenied(String message, SagaBase object) {
        super(message, object);
    }
}
