package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import org.apache.log4j.Logger;

import java.util.Calendar;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FilteredJobStatusPoller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FilteredJobStatusPoller extends AbstractJobStatusPoller {
    private static Logger s_logger = Logger.getLogger(FilteredJobStatusPoller.class);
    private QueryFilteredJob m_adaptor;

    public FilteredJobStatusPoller(QueryFilteredJob adaptor) {
        super();
        m_adaptor = adaptor;
    }

    public void run() {
        Object[] filters = new Object[3];
        filters[QueryFilteredJob.USER_ID] = null;           //todo: set filter value
        filters[QueryFilteredJob.COLLECTION_NAME] = null;   //todo: set filter value
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        filters[QueryFilteredJob.START_DATE] = cal.getTime();
        try {
            JobStatus[] statusArray = m_adaptor.getFilteredStatus(filters);
            for (JobStatus status : statusArray) {
                String nativeJobId = status.getNativeJobId();
                JobMonitorCallback callback;
                synchronized(m_subscribedJobs) {
                    callback = m_subscribedJobs.get(nativeJobId);
                }
                if (callback != null) {
                    callback.setState(status.getSagaState(), status.getStateDetail(), status.getSubState());
                }
            }
        } catch (Exception e) {
            s_logger.warn("Failed to get status for filtered jobs", e);
        }
    }
}
