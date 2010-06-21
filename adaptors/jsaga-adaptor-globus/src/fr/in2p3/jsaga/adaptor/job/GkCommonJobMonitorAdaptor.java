package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.apache.log4j.Logger;
import org.globus.gram.*;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GkCommonJobMonitorAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   21 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class GkCommonJobMonitorAdaptor extends GatekeeperJobAdaptorAbstract implements QueryIndividualJob {
	private Logger logger = Logger.getLogger(GkCommonJobMonitorAdaptor.class.getName());

    public String getType() {
        return "gk";
    }

    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        GramJob job = getGramJobById(nativeJobId);
        try {
        	Gram.jobStatus(job);
        } catch (GramException e) {
            if (e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER) {
                //WARNING: Globus does not distinguish job DONE and job manager stopped
            	logger.warn("Globus job manager may be stopped: status DONE returned in getStatus() for job "+nativeJobId);
                return new GatekeeperJobStatus(nativeJobId, new Integer(GRAMConstants.STATUS_DONE), "DONE");
            } else {
                this.rethrowException(e);
            }
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
        return new GatekeeperJobStatus(nativeJobId, new Integer(job.getStatus()), job.getStatusAsString(), job.getError());
    }

    protected void rethrowException(GramException e) throws TimeoutException, NoSuccessException {
        switch(e.getErrorCode()) {
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new TimeoutException(e);
            default:
                throw new NoSuccessException(e);
        }
    }
}
