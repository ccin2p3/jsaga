package fr.in2p3.jsaga.engine.job.monitor.request;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import org.ogf.saga.error.*;

import java.util.Calendar;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatusRequestor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobStatusRequestor {
    private JobMonitorAdaptor m_adaptor;

    public JobStatusRequestor(JobMonitorAdaptor adaptor) {
        m_adaptor = adaptor;
    }

    public boolean supportsQueryStatus() {
        return m_adaptor instanceof QueryJob;
    }

    public JobStatus getJobStatus(String nativeJobId) throws NotImplementedException, TimeoutException, NoSuccessException {
        if (nativeJobId != null) {
            try {
                if (m_adaptor instanceof QueryIndividualJob) {
                    return ((QueryIndividualJob) m_adaptor).getStatus(nativeJobId);
                } else if (m_adaptor instanceof QueryListJob) {
                    JobStatus[] statusArray = ((QueryListJob) m_adaptor).getStatusList(new String[]{nativeJobId});
                    return findJobStatus(statusArray, nativeJobId);
                } else if (m_adaptor instanceof QueryFilteredJob) {
                    Object[] filters = new Object[4];
                    filters[QueryFilteredJob.USER_ID] = null;           //todo: set filter value
                    filters[QueryFilteredJob.COLLECTION_NAME] = null;   //todo: set filter value
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    filters[QueryFilteredJob.START_DATE] = cal.getTime();
                    filters[QueryFilteredJob.JOB_ID] = nativeJobId;
                    JobStatus[] statusArray = ((QueryFilteredJob) m_adaptor).getFilteredStatus(filters);
                    return findJobStatus(statusArray, nativeJobId);
                } else if (m_adaptor instanceof ListenJob) {
                    throw new NoSuccessException("This adaptor requires the use of method waitFor() prior to calling method getState(): "+ m_adaptor.getClass().getName());
                } else {
                    throw new NotImplementedException("Querying job status not implemented for adaptor: "+ m_adaptor.getClass().getName());
                }
            } catch(RuntimeException e) {
                throw new NoSuccessException("Failed to get status for job: "+nativeJobId, e);
            }
        } else {
            return new JobStatus(nativeJobId, new Integer(0), "Unknown"){
                public String getModel() {return "Unknown";}
                public SubState getSubState() {return SubState.NEW_CREATED;}
            };
        }
    }

    private static JobStatus findJobStatus(JobStatus[] array, String nativeJobId) throws NoSuccessException {
        for (int i=0; array!=null && i<array.length; i++) {
            if (array[i].getNativeJobId().equals(nativeJobId)) {
                return array[i];
            }
        }
        throw new NoSuccessException("Job not found: "+nativeJobId);
    }
}
