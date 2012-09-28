package org.glite.security.authz;

/**
 * This exception is thrown when congiguration is missing or incorrect.
 */
public class ConfigException extends InitializeException {
    /**
     * Constructor.
     * @param message the error message
     */
    public ConfigException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param message the error message
     * @param cause the chained exception
     */
    public ConfigException(String message, Exception cause) {
        super(message, cause);
    }
}
