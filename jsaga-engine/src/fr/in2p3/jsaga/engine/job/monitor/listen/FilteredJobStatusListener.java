package fr.in2p3.jsaga.engine.job.monitor.listen;

import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.request.JobStatusRequestor;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FilteredJobStatusListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FilteredJobStatusListener extends AbstractJobStatusListener implements JobStatusNotifier {
    private ListenFilteredJob m_adaptor;
    private Map m_subscribedJobs;

    /** constructor */
    public FilteredJobStatusListener(ListenFilteredJob adaptor, JobStatusRequestor requestor) throws Timeout, NoSuccess {
        super(requestor);
        m_adaptor = adaptor;
        m_adaptor.subscribeFilteredJob(this);
        m_subscribedJobs = new HashMap();
    }

    /** destructor */
    protected void finalize() throws Throwable {
        super.finalize();
        m_adaptor.unsubscribeFilteredJob();
    }

    protected synchronized void doSubscribeJob(String nativeJobId, JobMonitorCallback callback) throws Timeout, NoSuccess {
        m_subscribedJobs.put(nativeJobId, callback);
    }

    public synchronized void unsubscribeJob(String nativeJobId) throws Timeout, NoSuccess {
        m_subscribedJobs.remove(nativeJobId);
    }

    public void notifyChange(JobStatus status) {
        JobMonitorCallback callback = (JobMonitorCallback) m_subscribedJobs.get(status.getNativeJobId());
        if (callback != null) {
            callback.setState(status.getSagaState(), status.getStateDetail());
        }
    }
}
