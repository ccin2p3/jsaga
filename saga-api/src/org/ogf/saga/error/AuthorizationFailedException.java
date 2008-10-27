package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a method fails because none of the available
 * session contexts could succesfully be used for authorization.
 */
public class AuthorizationFailedException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AuthorizationFailed exception.
     */
    public AuthorizationFailedException() {
        super(AUTHORIZATION_FAILED);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public AuthorizationFailedException(String message) {
        super(AUTHORIZATION_FAILED, message);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public AuthorizationFailedException(Throwable cause) {
        super(AUTHORIZATION_FAILED, cause);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public AuthorizationFailedException(String message, Throwable cause) {
        super(AUTHORIZATION_FAILED, message, cause);
    }

    /**
     * Constructs an AuthorizationFailed exception with the specified detail
     * message and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public AuthorizationFailedException(String message, SagaObject object) {
        super(AUTHORIZATION_FAILED, message, object);
    }    

    /**
     * Constructs a new AuthorizationFailedException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AuthorizationFailedException(Throwable cause, SagaObject object) {
        super(AUTHORIZATION_FAILED, cause, object);
    }

    /**
     * Constructs a new AuthorizationFailedException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AuthorizationFailedException(String detail, Throwable cause, SagaObject object) {
        super(AUTHORIZATION_FAILED, detail, cause, object);
    }

}
