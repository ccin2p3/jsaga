package fr.in2p3.jsaga.engine.job.monitor;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

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
    public void subscribeJob(String nativeJobId, JobMonitorCallback callback) throws Timeout, NoSuccess;
    public void unsubscribeJob(String nativeJobId) throws Timeout, NoSuccess;
}
