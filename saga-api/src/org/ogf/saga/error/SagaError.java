package org.ogf.saga.error;

/**
 * This is an unchecked exception, thrown only when a saga implementation could
 * not be loaded for some reason.
 */
public class SagaError extends java.lang.RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new SAGA error.
     */
    public SagaError() {
    }

    /**
     * Constructs a new SAGA error with the specified detail
     * message.
     * @param message the detail message.
     */
    public SagaError(String message) {
        super(message);
    }

    /**
     * Constructs a new SAGA error with the specified cause.
     * @param cause the cause.
     */
    public SagaError(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new SAGA error with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public SagaError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the message as specified by the SAGA API, i.e.,
     * <exception name>: <message>.
     * @return the message.
     */
    public String getMessage() {
        return this.getClass().getSimpleName() + ": "
                + super.getMessage();
    }
}
