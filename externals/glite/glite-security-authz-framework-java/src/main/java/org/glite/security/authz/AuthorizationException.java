package org.glite.security.authz;

/**
 * This exception is thrown to stop the evaluation of an authorization chain.
 */
public class AuthorizationException extends Exception {
    /**
     * Constructor.
     * @param message the error message
     */
    public AuthorizationException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public AuthorizationException(String message, Exception cause) {
        super(message, cause);
    }
}
