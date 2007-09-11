package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobAdaptor extends SagaSecureAdaptor {
    /**
     * @return the job service type.
     */
    public String getType();

    /**
     * @return the protocol schemes supported by sandbox management, or null if no sandbox management.
     */
    public String[] getSupportedSandboxProtocols();
}
