package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that one or more of the parameters of an operation
 * are ill-formed, invalid, out of bound, or otherwise not usable.
 */
public class BadParameterException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BadParameter exception.
     */
    public BadParameterException() {
        super(BAD_PARAMETER);
    }

    /**
     * Constructs a BadParameter exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public BadParameterException(String message) {
        super(BAD_PARAMETER, message);
    }

    /**
     * Constructs a BadParameter exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public BadParameterException(Throwable cause) {
        super(BAD_PARAMETER, cause);
    }

    /**
     * Constructs a BadParameter exception with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public BadParameterException(String message, Throwable cause) {
        super(BAD_PARAMETER, message, cause);
    }

    /**
     * Constructs a BadParameter exception with the specified detail message and
     * associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public BadParameterException(String message, SagaObject object) {
        super(BAD_PARAMETER, message, object);
    }
    
    /**
     * Constructs a new BadParameterException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public BadParameterException(Throwable cause, SagaObject object) {
        super(BAD_PARAMETER, cause, object);
    }

    /**
     * Constructs a new BadParameterExceptio with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public BadParameterException(String detail, Throwable cause, SagaObject object) {
        super(BAD_PARAMETER, detail, cause, object);
    }

}
