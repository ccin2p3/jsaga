package fr.in2p3.jsaga.adaptor;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaBaseAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SagaBaseAdaptor {
    /**
     * @return list of supported security context types.
     */
    public String[] getSupportedContextTypes();

    /**
     * Set the security adaptor.
     * @param securityAdaptor the security adaptor.
     */
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor);
}
