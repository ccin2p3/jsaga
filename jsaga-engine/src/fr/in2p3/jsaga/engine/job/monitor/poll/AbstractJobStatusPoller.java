package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobRegistry;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobStatusPoller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobStatusPoller extends TimerTask implements JobRegistry {
    protected Map m_subscribedJobs;

    public AbstractJobStatusPoller() {
        m_subscribedJobs = new HashMap();
    }

    public synchronized void subscribeJob(String nativeJobId, JobMonitorCallback callback) {
        m_subscribedJobs.put(nativeJobId, callback);
    }

    public synchronized void unsubscribeJob(String nativeJobId) {
        m_subscribedJobs.remove(nativeJobId);
    }
}
