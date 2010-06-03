package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.Map.Entry;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IndividualJobStatusPoller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IndividualJobStatusPoller extends AbstractJobStatusPoller {
    private static Logger s_logger = Logger.getLogger(IndividualJobStatusPoller.class);
    private QueryIndividualJob m_adaptor;

    public IndividualJobStatusPoller(QueryIndividualJob adaptor) {
        super();
        m_adaptor = adaptor;
    }

    public void run() {
        //TODO: should be multi-threaded
        Set<Entry<String, JobMonitorCallback>> entries;
        synchronized(m_subscribedJobs) {
            entries = m_subscribedJobs.entrySet();
        }
        for (Entry<String, JobMonitorCallback> entry : entries) {
            String nativeJobId = (String) entry.getKey();
            JobMonitorCallback callback = (JobMonitorCallback) entry.getValue();
            try {
                JobStatus status = m_adaptor.getStatus(nativeJobId);
                callback.setState(status.getSagaState(), status.getStateDetail(), status.getSubState(), status.getCause());
            } catch (Exception e) {
                s_logger.warn("Failed to get status for job: "+ nativeJobId, e);
            }
        }
    }
}
