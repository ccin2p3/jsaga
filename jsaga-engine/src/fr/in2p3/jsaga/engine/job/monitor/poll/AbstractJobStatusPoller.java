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
    protected final Map<String,JobMonitorCallback> m_subscribedJobs;

    public AbstractJobStatusPoller() {
        m_subscribedJobs = new HashMap<String,JobMonitorCallback>();
    }

    public void subscribeJob(String nativeJobId, JobMonitorCallback callback) {
        synchronized(m_subscribedJobs) {
            m_subscribedJobs.put(nativeJobId, callback);
        }
    }

    public void unsubscribeJob(String nativeJobId) {
        synchronized(m_subscribedJobs) {
            m_subscribedJobs.remove(nativeJobId);
        }
    }
}
