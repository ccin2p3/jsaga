package fr.in2p3.jsaga.impl.job.service;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.SagaException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ReconnectionException
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class ReconnectionException extends NoSuccessException {
    public ReconnectionException(SagaException exception) {
        super("Failed to reconnect to job service after proxy renewal", exception);
    }
}
