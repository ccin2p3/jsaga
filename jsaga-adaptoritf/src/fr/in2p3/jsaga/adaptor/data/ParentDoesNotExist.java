package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.SagaException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ParentDoesNotExist
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ParentDoesNotExist extends SagaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DoesNotExistException exception.
     */
    public ParentDoesNotExist() {
        super(DOES_NOT_EXIST);
    }

    /**
     * Constructs a ParentDoesNotExist exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public ParentDoesNotExist(String message) {
        super(DOES_NOT_EXIST, message);
    }

    /**
     * Constructs a ParentDoesNotExist exception with the specified cause.
     * @param cause the cause.
     */
    public ParentDoesNotExist(Throwable cause) {
        super(DOES_NOT_EXIST, cause);
    }

    /**
     * Constructs a ParentDoesNotExist exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     *
     */
    public ParentDoesNotExist(String message, Throwable cause) {
        super(DOES_NOT_EXIST, message, cause);
    }

    /**
     * Constructs a ParentDoesNotExist exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public ParentDoesNotExist(String message, SagaObject object) {
        super(DOES_NOT_EXIST, message, object);
    }
}
