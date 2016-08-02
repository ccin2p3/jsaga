package fr.in2p3.jsaga.engine.job.monitor;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRegistry
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobRegistry {
    public void subscribeJob(String nativeJobId, JobMonitorCallback callback) throws NotImplementedException, TimeoutException, NoSuccessException;
    public void unsubscribeJob(String nativeJobId) throws NotImplementedException, TimeoutException, NoSuccessException;
}
