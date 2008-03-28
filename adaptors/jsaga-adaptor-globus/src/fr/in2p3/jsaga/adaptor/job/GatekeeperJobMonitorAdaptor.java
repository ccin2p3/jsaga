package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.*;

import org.apache.log4j.Logger;
import org.globus.gram.*;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import java.net.MalformedURLException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobMonitorAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GatekeeperJobMonitorAdaptor extends GatekeeperJobAdaptorAbstract implements QueryIndividualJob, ListenIndividualJob {
	private Logger logger = Logger.getLogger(GatekeeperJobMonitorAdaptor.class.getName());
	
    public String getType() {
        return "gatekeeper";
    }

    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }

    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {    	
        GramJob job = this.getGramJobById(nativeJobId);
        try {
        	Gram.jobStatus(job);
        	// TODO : move to cleanup
        	if(job.getStatus() == GRAMConstants.STATUS_DONE ||
        			job.getStatus() == GRAMConstants.STATUS_FAILED) {
        		try {
        			// Send signal to clean jobmanager
    				job.signal(GRAMConstants.SIGNAL_COMMIT_END);
    			} catch (GramException e) {
    				e.printStackTrace();
    			} catch (GSSException e) {
    				e.printStackTrace();
    			}
        	}
        } catch (GramException e) {
            if (e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER) {
                //WARNING: Globus does not distinguish job DONE and job manager stopped
            	logger.warn("Globus job manager may be stopped: status DONE returned in getStatus()");
                return new GatekeeperJobStatus(nativeJobId, new Integer(GRAMConstants.STATUS_DONE), "DONE");
            } else {
                this.rethrowException(e);
            }
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        return new GatekeeperJobStatus(nativeJobId, new Integer(job.getStatus()), job.getStatusAsString());
    }

    public void subscribeJob(String nativeJobId, JobStatusNotifier notifier) throws Timeout, NoSuccess {
        GramJob job = this.getGramJobById(nativeJobId);
        GramJobListener listener = new GatekeeperJobStatusListener(notifier);
        job.addListener(listener);
        try {
        	job.bind();
        } catch (GramException e) {
        	job.removeListener(listener);
            if (e.getErrorCode() == GramException.CONNECTION_FAILED ||
            		e.getErrorCode() == GramException.JOB_QUERY_DENIAL ||
            		e.getErrorCode() == GramException.HTTP_UNFRAME_FAILED) {
                //WARNING: Globus does not distinguish job DONE and job manager stopped
            	logger.warn("Globus job manager may be stopped: status DONE returned in subscribeJob()");
                GatekeeperJobStatus status = new GatekeeperJobStatus(nativeJobId, new Integer(GRAMConstants.STATUS_DONE), "DONE");
                notifier.notifyChange(status);
            } else {
                this.rethrowException(e);
            }
        } catch (GSSException e) {
            job.removeListener(listener);
            throw new NoSuccess(e);
        }
    }

    public void unsubscribeJob(String nativeJobId) throws Timeout, NoSuccess {
        GramJob job = this.getGramJobById(nativeJobId);
        try {
            job.unbind();
        } catch (GramException e) {
        	if (e.getErrorCode() == GramException.CONNECTION_FAILED ||
            		e.getErrorCode() == GramException.JOB_QUERY_DENIAL ||
            		e.getErrorCode() == GramException.HTTP_UNFRAME_FAILED) {
        		logger.warn("Globus job manager may be stopped: status DONE returned in unsubscribeJob()");
                // ignore (Globus does not distinguish job DONE and job manager stopped)
            } else {
                this.rethrowException(e);
            }
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        //NOTICE: no need to remove GramJobListener since job is unregistered from CallbackHandler
    }

    private GramJob getGramJobById(String nativeJobId) throws NoSuccess {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
        return job;
    }

    private void rethrowException(GramException e) throws Timeout, NoSuccess {
        switch(e.getErrorCode()) {
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new Timeout(e);
            default:
                throw new NoSuccess(e);
        }
    }
}
