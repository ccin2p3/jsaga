package org.glite.security.authz;

/**
 * This exception is thrown when an exception occured during attribute
 * collection.
 */
public class AttributeException extends AuthorizationException {
    /**
     * Constructor.
     * @param message the error message
     */
    public AttributeException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public AttributeException(String message, Exception cause) {
        super(message, cause);
    }
}
