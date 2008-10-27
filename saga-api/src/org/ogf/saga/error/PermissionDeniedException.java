package org.ogf.saga.error;

import org.ogf.saga.SagaObject;

/**
 * This exception indicates that the identity used for the operation did not
 * have sufficient permissions to perform the operation successfully.
 */
public class PermissionDeniedException extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a PermissionDenied exception.
     */
    public PermissionDeniedException() {
        super(PERMISSION_DENIED);
    }

    /**
     * Constructs a PermissionDenied exception with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public PermissionDeniedException(String message) {
        super(PERMISSION_DENIED, message);
    }

    /**
     * Constructs a PermissionDenied exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public PermissionDeniedException(Throwable cause) {
        super(PERMISSION_DENIED, cause);
    }

    /**
     * Constructs a PermissionDenied exception with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public PermissionDeniedException(String message, Throwable cause) {
        super(PERMISSION_DENIED, message, cause);
    }

    /**
     * Constructs a PermissionDenied exception with the specified detail message
     * and associated SAGA object.
     * 
     * @param message
     *            the detail message.
     * @param object
     *            the associated SAGA object.
     */
    public PermissionDeniedException(String message, SagaObject object) {
        super(PERMISSION_DENIED, message, object);
    }

    /**
     * Constructs a new PermissionDeniedException with the specified cause and
     * associated SAGA object.
     * 
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public PermissionDeniedException(Throwable cause, SagaObject object) {
        super(PERMISSION_DENIED, cause, object);
    }

    /**
     * Constructs a new PermissionDeniedException with the specified detail message, 
     * specified cause and associated SAGA object.
     * 
     * @param detail
     *            the detail message.
     * @param cause
     *            the cause.
     * @param object
     *            the SAGA object associated with the exception.
     */
    public PermissionDeniedException(String detail, Throwable cause, SagaObject object) {
        super(PERMISSION_DENIED, detail, cause, object);
    }

}
