package fr.in2p3.jsaga.adaptor.base;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaSecureAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SagaSecureAdaptor extends SagaBaseAdaptor {
    /**
     * @return list of supported SecurityAdaptor classes.
     */
    public Class[] getSupportedSecurityAdaptorClasses();

    /**
     * Set the security adaptor.
     * @param securityAdaptor the security adaptor.
     */
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor);
}
