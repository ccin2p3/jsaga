package fr.in2p3.jsaga.engine.job.monitor.listen;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobRegistry;
import fr.in2p3.jsaga.engine.job.monitor.request.JobStatusRequestor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobStatusListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobStatusListener implements JobRegistry {
    protected JobStatusRequestor m_requestor;

    public AbstractJobStatusListener(JobStatusRequestor requestor) {
        m_requestor = requestor;
    }

    protected abstract void doSubscribeJob(String nativeJobId, JobMonitorCallback callback) throws Timeout, NoSuccess;

    public void subscribeJob(String nativeJobId, JobMonitorCallback callback) throws Timeout, NoSuccess {
        // try to notify initial status
        try {
            JobStatus status = m_requestor.getJobStatus(nativeJobId);
            callback.setState(status.getSagaState(), status.getStateDetail());
        } catch (NotImplemented e) {
            // ignore if no QueryJob interface is implemented
        }

        // subscribe to next status changes
        this.doSubscribeJob(nativeJobId, callback);
    }
}
