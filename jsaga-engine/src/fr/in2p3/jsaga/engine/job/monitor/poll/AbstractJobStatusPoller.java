package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobRegistry;

import java.util.HashMap;
import java.util.Map;

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
public abstract class AbstractJobStatusPoller implements JobRegistry, Runnable {
    protected final Map<String,JobMonitorCallback> m_subscribedJobs;
    private JobStatusPollerTask m_pollerTask;

    public AbstractJobStatusPoller() {
        m_subscribedJobs = new HashMap<String,JobMonitorCallback>();
    }

    public void subscribeJob(String nativeJobId, JobMonitorCallback callback) {
        synchronized(m_subscribedJobs) {
            boolean toBeStarted = m_subscribedJobs.isEmpty();

            // subscribe job
            m_subscribedJobs.put(nativeJobId, callback);

            // may start timer
            if (toBeStarted) {
                m_pollerTask = new JobStatusPollerTask(this);
                m_pollerTask.start();
            }
        }
    }

    public void unsubscribeJob(String nativeJobId) {
        synchronized(m_subscribedJobs) {
            // unsubscribe job
            m_subscribedJobs.remove(nativeJobId);

            // may stop timer
            if (m_subscribedJobs.isEmpty() && m_pollerTask!=null) {
                m_pollerTask.stop();
                m_pollerTask = null;
            }
        }
    }
}

