package org.glite.security.authz;

/**
 * This exception is thrown when an exception occurs during initialization.
 */
public class InitializeException extends Exception {
    /**
     * Constructor.
     * @param message the error message
     */
    public InitializeException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public InitializeException(String message, Exception cause) {
        super(message, cause);
    }
}
