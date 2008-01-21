package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log s_logger = LogFactory.getLog(ListJobStatusPoller.class);
    private QueryListJob m_adaptor;

    public ListJobStatusPoller(QueryListJob adaptor) {
        super();
        m_adaptor = adaptor;
    }

    public void run() {
        String[] jobsToQuery;
        synchronized(this) {
            jobsToQuery = (String[]) m_subscribedJobs.keySet().toArray(new String[m_subscribedJobs.size()]);
        }
        try {
            JobStatus[] statusArray = m_adaptor.getStatusList(jobsToQuery);
            for (int i=0; i<statusArray.length; i++) {
                String nativeJobId = statusArray[i].getNativeJobId();
                JobMonitorCallback callback = (JobMonitorCallback) m_subscribedJobs.get(nativeJobId);
                if (callback != null) {
                    JobStatus status = statusArray[i];
                    callback.setState(status.getSagaState(), status.getStateDetail());
                }
            }
        } catch (Exception e) {
            s_logger.warn("Failed to get status for list of jobs");
        }
    }
}
