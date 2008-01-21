package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This is the base class for all exceptions in SAGA.
 * It is a checked exception, so all exceptions in SAGA are
 * checked exceptions.
 */
public abstract class Exception extends java.lang.Exception implements Comparable<Exception> {

    // Determine the order of the exceptions, most specific first.
    protected static final int NOT_IMPLEMENTED = 0;
    protected static final int INCORRECT_URL = 1;
    protected static final int BAD_PARAMETER = 2;
    protected static final int ALREADY_EXISTS = 3;
    protected static final int DOES_NOT_EXIST = 4;
    protected static final int INCORRECT_STATE = 5;
    protected static final int PERMISSION_DENIED = 6;
    protected static final int AUTHORIZATION_FAILED = 7;
    protected static final int AUTHENTICATION_FAILED = 8;
    protected static final int TIMEOUT = 9;
    protected static final int NO_SUCCESS = 10;
    
    /** Determines how specific the exception is with respect to others. */
    private final int exceptionOrder;
    
    private static final long serialVersionUID = 1L;
    
    private transient final SagaObject object;

    /**
     * Constructs a new SAGA exception.
     * @param order initializes the exceptionOrder field that determines
     *    which exception is more specific.
     */
    public Exception(int order) {
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message.
     * @param order initializes the exceptionOrder field that determines
     *    which exception is more specific.
     * @param message the detail message.
     */
    public Exception(int order, String message) {
        super(message);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified cause.
     * @param order initializes the exceptionOrder field that determines
     *    which exception is more specific.
     * @param cause the cause.
     */
    public Exception(int order, Throwable cause) {
        super(cause);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message and cause.
     * @param order initializes the exceptionOrder field that determines
     *    which exception is more specific.
     * @param message the detail message.
     * @param cause the cause.
     */
    public Exception(int order, String message, Throwable cause) {
        super(message, cause);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail
     * message and associated SAGA object.
     * @param order initializes the exceptionOrder field that determines
     *    which exception is more specific.
     * @param message the detail message.
     * @param object the SAGA object associated with the exception.
     */
    public Exception(int order, String message, SagaObject object) {
        super(message);
        this.object = object;
        exceptionOrder = order;
    }
    
    /**
     * Returns the SAGA object associated with this exception.
     * @return the associated SAGA object.
     */
    public SagaObject getObject() throws DoesNotExist, NoSuccess {
        if (object == null) {
            throw new DoesNotExist("No object associated with this exception");
        }
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
   
    /**
     * Gives preference to the most specific exception.
     * This implements the ordering as in the SAGA specs.
     * Returns < 0 if this exception is more specific than the specified exception,
     * 0 if equal, and > 0 if less.
     */
    public int compareTo(Exception o) {
        return exceptionOrder - o.exceptionOrder;
    }
    
}
