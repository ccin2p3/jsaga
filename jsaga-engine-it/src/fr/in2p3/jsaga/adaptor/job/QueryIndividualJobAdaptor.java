package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.impl.JobAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   QueryIndividualJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class QueryIndividualJobAdaptor extends JobAdaptorAbstract implements QueryIndividualJob {
    public String getType() {
        return "query-individual";
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        return new JobStatus(nativeJobId, null, null){
            public String getModel() {return "TEST";}
            public SubState getSubState() {return SubState.DONE;}
        };
    }
}
