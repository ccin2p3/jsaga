package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LCGCEJobMonitorAdaptor extends GatekeeperJobAdaptorAbstract implements QueryIndividualJob {
    public String getType() {
        return "lcgce";
    }

    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        throw new NoSuccessException("Job monitoring not supported by this adaptor");
    }
}
