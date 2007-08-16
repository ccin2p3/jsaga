package fr.in2p3.jsaga.engine.config;

import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AmbiguityException
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AmbiguityException extends NoSuccess {
    public AmbiguityException(String message) {
        super(message);
    }

    public AmbiguityException(Throwable throwable) {
        super(throwable);
    }
}
