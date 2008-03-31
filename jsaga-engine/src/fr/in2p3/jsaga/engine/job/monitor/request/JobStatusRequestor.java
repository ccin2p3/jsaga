package fr.in2p3.jsaga.engine.job.monitor.request;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import org.ogf.saga.error.*;

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

    public JobStatus getJobStatus(String nativeJobId) throws NotImplemented, Timeout, NoSuccess {
        if (nativeJobId != null) {
            if (m_adaptor instanceof QueryIndividualJob) {
                return ((QueryIndividualJob) m_adaptor).getStatus(nativeJobId);
            } else if (m_adaptor instanceof QueryListJob) {
                JobStatus[] statusArray = ((QueryListJob) m_adaptor).getStatusList(new String[]{nativeJobId});
                return findJobStatus(statusArray, nativeJobId);
            } else if (m_adaptor instanceof QueryFilteredJob) {
                //todo: set filter string (e.g. userID, jcName, startDate) ?
                JobStatus[] statusArray = ((QueryFilteredJob) m_adaptor).getFilteredStatus(null, null, null);
                return findJobStatus(statusArray, nativeJobId);
            } else {
                throw new NotImplemented("Querying job status not implemented for adaptor: "+ m_adaptor.getClass().getName());
            }
        } else {
            return new JobStatus(nativeJobId, new Integer(0), "Unknown"){
                public String getModel() {return "Unknown";}
                public SubState getSubState() {return SubState.SUBMITTED;}
            };
        }
    }

    private static JobStatus findJobStatus(JobStatus[] array, String nativeJobId) throws NoSuccess {
        for (int i=0; array!=null && i<array.length; i++) {
            if (array[i].getNativeJobId().equals(nativeJobId)) {
                return array[i];
            }
        }
        throw new NoSuccess("Job not found: "+nativeJobId);
    }
}
