package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that a method fails because none of the available
 * session contexts could succesfully be used for authentication.
 */
public class AuthenticationFailedException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AuthenticationFailed exception.
     */
    public AuthenticationFailedException() {
        super(AUTHENTICATION_FAILED);
    }

    /**
     * Constructs an AuthenticationFailed exception with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public AuthenticationFailedException(String message) {
        super(AUTHENTICATION_FAILED, message);
    }

    /**
     * Constructs an AuthenticationFailed exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public AuthenticationFailedException(Throwable cause) {
        super(AUTHENTICATION_FAILED, cause);
    }

    /**
     * Constructs an AuthenticationFailed exception with the specified detail
     * message and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public AuthenticationFailedException(String message, Throwable cause) {
        super(AUTHENTICATION_FAILED, message, cause);
    }

    /**
     * Constructs an AuthenticationFailed exception with the specified detail
     * message and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public AuthenticationFailedException(String message, SagaObject object) {
        super(AUTHENTICATION_FAILED, message, object);
    }
    
    /**
     * Constructs a new AuthenticationFailedException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AuthenticationFailedException(Throwable cause, SagaObject object) {
        super(AUTHENTICATION_FAILED, cause, object);
    }

    /**
     * Constructs a new AuthenticationFailedException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public AuthenticationFailedException(String detail, Throwable cause, SagaObject object) {
        super(AUTHENTICATION_FAILED, detail, cause, object);
    }

}
