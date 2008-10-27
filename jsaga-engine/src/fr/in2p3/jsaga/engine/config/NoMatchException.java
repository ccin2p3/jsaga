package fr.in2p3.jsaga.engine.config;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NoMatchException
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NoMatchException extends NoSuccessException {
    public NoMatchException(String id, String message) {
        super("["+id+"] "+message);
    }

    public NoMatchException(Throwable throwable) {
        super(throwable);
    }
}
