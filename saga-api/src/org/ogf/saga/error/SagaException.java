package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This is the base class for all exceptions in SAGA. It is a checked exception,
 * so all exceptions in SAGA are checked exceptions.
 * The {@link #getMessage} does not quite behave as specified in the SAGA specs, because
 * Java already has {@link #toString} for that.
 * So, {@link #getMessage} does what it always does for Java throwables.
 */
public abstract class SagaException extends Exception implements
        Comparable<SagaException> {

    // Determine the order of the exceptions, most specific first.
    protected static final int NOT_IMPLEMENTED = 0;

    protected static final int IO_EXCEPTION = 1;

    protected static final int INCORRECT_URL = 2;

    protected static final int BAD_PARAMETER = 3;

    protected static final int ALREADY_EXISTS = 4;

    protected static final int DOES_NOT_EXIST = 5;

    protected static final int INCORRECT_STATE = 6;

    protected static final int PERMISSION_DENIED = 7;

    protected static final int AUTHORIZATION_FAILED = 8;

    protected static final int AUTHENTICATION_FAILED = 9;

    protected static final int TIMEOUT = 10;

    protected static final int NO_SUCCESS = 11;

    /** Determines how specific the exception is with respect to others. */
    private final int exceptionOrder;

    private static final long serialVersionUID = 1L;

    private transient final SagaObject object;

    /**
     * Constructs a new SAGA exception.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     */
    protected SagaException(int order) {
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail message.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param message
     *            the detail message.
     */
    protected SagaException(int order, String message) {
        super(message);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified cause.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param cause
     *            the cause.
     */
    protected SagaException(int order, Throwable cause) {
        super(cause);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail message and
     * cause.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    protected SagaException(int order, String message, Throwable cause) {
        super(message, cause);
        object = null;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail message and
     * associated SAGA object.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param message
     *            the detail message.
     * @param object
     *            the SAGA object associated with the exception.
     */
    protected SagaException(int order, String message, SagaObject object) {
        super(message);
        this.object = object;
        exceptionOrder = order;
    }
    

    /**
     * Constructs a new SAGA exception with the specified cause and
     * associated SAGA object.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    protected SagaException(int order, Throwable cause, SagaObject object) {
        super(cause);
        this.object = object;
        exceptionOrder = order;
    }

    /**
     * Constructs a new SAGA exception with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param order
     *            initializes the exceptionOrder field that determines which
     *            exception is more specific.
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    protected SagaException(int order, String detail, Throwable cause, SagaObject object) {
        super(detail, cause);
        this.object = object;
        exceptionOrder = order;
    }

    /**
     * Returns the SAGA object associated with this exception.
     * 
     * @return the associated SAGA object.
     */
    public SagaObject getObject() throws DoesNotExistException,
            NoSuccessException {
        if (object == null) {
            throw new DoesNotExistException(
                    "No object associated with this exception");
        }
        return object;
    }

    /**
     * Gives preference to the most specific exception. This implements the
     * ordering as in the SAGA specs. Returns < 0 if this exception is more
     * specific than the specified exception, 0 if equal, and > 0 if less.
     */
    public int compareTo(SagaException o) {
        return exceptionOrder - o.exceptionOrder;
    }
    
    /**
     * Returns a short description of this <code>SagaException</code>.
     * If this <code>SagaException</code> object was created with a non-null detail message string,
     * then the result is the concatenation of three strings:
     * <ul>
     * <li>The simple (unqualified) name of the actual class of this object
     * <li>": " (a colon and a space)
     * <li>The result of the {@link #getMessage} method for this object
     * </ul> 
     * If this <code>SagaException</code> object was created with a null detail message string,
     * then only the simple (unqualified) name of the actual class of this object is returned.
     * 
     * @return a string representation of this <code>SagaException</code>.
     */
    public String toString() {
        String message = getMessage();
        String result = getClass().getSimpleName();
        
        if (result.endsWith("Exception")) {
            result = result.replace("Exception", "");
        }
        
        if (message != null) {
            result = result + ": " + message;
        }
        return result;
    }

}
