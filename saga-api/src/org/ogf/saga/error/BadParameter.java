package org.ogf.saga.error;

import org.ogf.saga.SagaBase;

/**
 * This exception indicates that one or more of the parameters of an
 * operation are ill-formed, invalid, out of bound, or otherwise not
 * usable.
 */
public class BadParameter extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BadParameter exception.
     */
    public BadParameter() {
    }

    /**
     * Constructs a BadParameter exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public BadParameter(String message) {
        super(message);
    }

    /**
     * Constructs a BadParameter exception with the specified cause.
     * @param cause the cause.
     */
    public BadParameter(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BadParameter exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * 
     */
    public BadParameter(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a BadParameter exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public BadParameter(String message, SagaBase object) {
        super(message, object);
    }
}
