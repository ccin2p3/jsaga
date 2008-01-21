package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that an operation cannot succeed because a
 * required entity is missing.
 */
public class DoesNotExist extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DoesNotExist exception.
     */
    public DoesNotExist() {
        super(DOES_NOT_EXIST);
    }

    /**
     * Constructs a DoesNotExist exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public DoesNotExist(String message) {
        super(DOES_NOT_EXIST, message);
    }

    /**
     * Constructs a DoesNotExist exception with the specified cause.
     * @param cause the cause.
     */
    public DoesNotExist(Throwable cause) {
        super(DOES_NOT_EXIST, cause);
    }

    /**
     * Constructs a DoesNotExist exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public DoesNotExist(String message, Throwable cause) {
        super(DOES_NOT_EXIST, message, cause);
    }
    
    /**
     * Constructs a DoesNotExist exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public DoesNotExist(String message, SagaObject object) {
        super(DOES_NOT_EXIST, message, object);
    }
}
