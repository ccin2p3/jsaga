package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This is the base class for all exceptions in SAGA.
 * It is a checked exception, so all exceptions in SAGA are
 * checked exceptions.
 */
public abstract class Exception extends java.lang.Exception {

    private static final long serialVersionUID = 1L;
    
    private transient final SagaObject object;

    /**
     * Constructs a new SAGA exception.
     */
    public Exception() {
        object = null;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public Exception(String message) {
        super(message);
        object = null;
    }

    /**
     * Constructs a new SAGA exception with the specified cause.
     * @param cause the cause.
     */
    public Exception(Throwable cause) {
        super(cause);
        object = null;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public Exception(String message, Throwable cause) {
        super(message, cause);
        object = null;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the SAGA object associated with the exception.
     */
    public Exception(String message, SagaObject object) {
        super(message);
        this.object = object;
    }
    
    /**
     * Returns the SAGA object associated with this exception, or
     * <code>null</code>.
     * @return the associated SAGA object.
     */
    public SagaObject getObject() {
        return object;
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
