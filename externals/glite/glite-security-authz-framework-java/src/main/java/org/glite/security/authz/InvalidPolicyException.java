package org.glite.security.authz;

/**
 * This exception is thrown when an invalid policy was found.
 */
public class InvalidPolicyException extends Exception {
    /**
     * Constructor.
     * @param message the error message
     */
    public InvalidPolicyException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public InvalidPolicyException(String message, Exception cause) {
        super(message, cause);
    }
}
