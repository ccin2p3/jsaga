package org.glite.ce.commonj.authz;

/**
 * This exception is thrown when an exception occured when closing down an
 * interceptor chain.
 */
public class CloseException extends Exception {
    /**
     * Constructor.
     * @param message the error message
     */
    public CloseException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public CloseException(String message, Exception cause) {
        super(message, cause);
    }
}
