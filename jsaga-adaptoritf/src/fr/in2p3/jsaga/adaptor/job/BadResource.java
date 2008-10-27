package fr.in2p3.jsaga.adaptor.job;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BadResource
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BadResource extends NoSuccessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BadResource exception.
     */
    public BadResource() {
        super();
    }

    /**
     * Constructs a BadResource exception with the specified detail
     * message.
     * @param message the detail message.
     */
    public BadResource(String message) {
        super(message);
    }

    /**
     * Constructs a BadResource exception with the specified cause.
     * @param cause the cause.
     */
    public BadResource(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BadResource exception with the specified detail
     * message and cause.
     * @param message the detail message.
     * @param cause the cause.
     *
     */
    public BadResource(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a BadResource exception with the specified detail
     * message and associated SAGA object.
     * @param message the detail message.
     * @param object the associated SAGA object.
     */
    public BadResource(String message, SagaObject object) {
        super(message, object);
    }
}
