package fr.in2p3.jsaga.engine.config;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ConfigurationException
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ConfigurationException extends NoSuccessException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
