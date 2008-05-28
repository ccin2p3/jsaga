package fr.in2p3.jsaga.adaptor.job.monitor;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobMonitorAdaptor extends SagaSecureAdaptor {
    /**
     * @return the default server port.
     */
    public int getDefaultPort();
}
