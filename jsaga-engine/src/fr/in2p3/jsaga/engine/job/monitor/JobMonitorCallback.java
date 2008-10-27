package fr.in2p3.jsaga.engine.job.monitor;

import fr.in2p3.jsaga.adaptor.job.SubState;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorCallback
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobMonitorCallback {
    public void setState(State state, String stateDetail, SubState subState, SagaException cause);
}
