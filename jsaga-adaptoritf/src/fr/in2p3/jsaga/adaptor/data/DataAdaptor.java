package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataAdaptor extends SagaSecureAdaptor {
    /**
     * @return the aliases of the protocol scheme supported by this adaptor.
     */
    public String[] getSchemeAliases();
}
