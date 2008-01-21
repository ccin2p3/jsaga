package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a method fails because none of the available
 * session contexts could succesfully be used for authorization.
 */
public class AuthorizationFailed extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AuthorizationFailed exception.
     */
    public AuthorizationFailed() {
        super(AUTHORIZATION_FAILED);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public AuthorizationFailed(String message) {
        super(AUTHORIZATION_FAILED, message);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified cause.
     * @param cause the cause.
     */
    public AuthorizationFailed(Throwable cause) {
        super(AUTHORIZATION_FAILED, cause);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message and cause. 
     * @param message the detail message.
     * @param cause the cause.
     */
    public AuthorizationFailed(String message, Throwable cause) {
        super(AUTHORIZATION_FAILED, message, cause);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public AuthorizationFailed(String message, SagaObject object) {
        super(AUTHORIZATION_FAILED, message, object);
    }
}
