package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import org.apache.log4j.Logger;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ListJobStatusPoller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ListJobStatusPoller extends AbstractJobStatusPoller {
    private static Logger s_logger = Logger.getLogger(ListJobStatusPoller.class);
    private QueryListJob m_adaptor;

    public ListJobStatusPoller(QueryListJob adaptor) {
        super();
        m_adaptor = adaptor;
    }

    public void run() {
        String[] jobsToQuery;
        synchronized(m_subscribedJobs) {
            jobsToQuery = m_subscribedJobs.keySet().toArray(new String[m_subscribedJobs.size()]);
        }
        try {
            JobStatus[] statusArray = m_adaptor.getStatusList(jobsToQuery);
            for (JobStatus status : statusArray) {
                String nativeJobId = status.getNativeJobId();
                JobMonitorCallback callback;
                synchronized(m_subscribedJobs) {
                    callback = m_subscribedJobs.get(nativeJobId);
                }
                if (callback != null) {
                    callback.setState(status.getSagaState(), status.getStateDetail(), status.getSubState(), status.getCause());
                }
            }
        } catch (Exception e) {
            s_logger.warn("Failed to get status for list of jobs", e);
        }
    }
}
