package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   QueryFilteredJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class QueryFilteredJobAdaptor extends JobAdaptorAbstract implements QueryFilteredJob {
    public String getType() {
        return "query-filtered";
    }

    public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException {
        return new JobStatus[] {
                new JobStatus(""+filters[QueryFilteredJob.JOB_ID], null, null){
                    public String getModel() {return "TEST";}
                    public SubState getSubState() {return SubState.DONE;}
                }
        };
    }
}
