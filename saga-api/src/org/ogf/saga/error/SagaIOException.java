package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception is specific to the Java Language Bindings for SAGA, which uses
 * this exception for some methods, instead of returning POSIX error code.
 */
public class SagaIOException extends SagaException {

    private static final long serialVersionUID = 1L;

    private final int posixErrorCode;

    /**
     * Constructs an SagaIO exception.
     */
    public SagaIOException() {
        super(IO_EXCEPTION);
        posixErrorCode = 0;
    }

    /**
     * Constructs an SagaIO exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public SagaIOException(String message) {
        super(IO_EXCEPTION, message);
        posixErrorCode = 0;
    }

    /**
     * Constructs an SagaIO exception with the specified detail message and
     * error code,
     * 
     * @param message
     *            the detail message.
     * @param code
     *            the error code.
     */
    public SagaIOException(String message, int code) {
        super(IO_EXCEPTION, message);
        posixErrorCode = code;
    }

    /**
     * Constructs an SagaIO exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public SagaIOException(Throwable cause) {
        super(IO_EXCEPTION, cause);
        posixErrorCode = 0;
    }

    /**
     * Constructs an SagaIO exception with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public SagaIOException(String message, Throwable cause) {
        super(IO_EXCEPTION, message, cause);
        posixErrorCode = 0;
    }

    /**
     * Constructs an SagaIO exception with the specified detail message and
     * associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public SagaIOException(String message, SagaObject object) {
        super(IO_EXCEPTION, message, object);
        posixErrorCode = 0;
    }
    

    /**
     * Constructs a new SagaIOException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public SagaIOException(Throwable cause, SagaObject object) {
        super(IO_EXCEPTION, cause, object);
        posixErrorCode = 0;
    }

    /**
     * Constructs a new SagaIOException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public SagaIOException(String detail, Throwable cause, SagaObject object) {
        super(IO_EXCEPTION, detail, cause, object);
        posixErrorCode = 0;
    }

    /**
     * Constructs an SagaIO exception with the specified detail message. POSIX
     * error code, and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param code
     *            the error code.
     * @param cause
     *            the cause.
     * @param object
     *            the associated SAGA object.
     */
    public SagaIOException(String message, Throwable cause, int code, SagaObject object) {
        super(IO_EXCEPTION, message, cause, object);
        posixErrorCode = code;
    }

    /**
     * Returns the POSIX error code associated with this exception, in case it
     * is available. If not, this method returns 0.
     * 
     * @return the error code.
     */
    public int getPosixErrorCode() {
        return posixErrorCode;
    }
}
