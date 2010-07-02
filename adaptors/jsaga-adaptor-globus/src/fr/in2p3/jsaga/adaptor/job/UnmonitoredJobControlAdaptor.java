package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.log4j.Logger;
import org.globus.gram.*;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.globus.rsl.*;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import java.net.MalformedURLException;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UnmonitoredJobControlAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   25 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 * NOTE: This is a limited version of the GkCommonJobControlAdaptor, which does not support job monitoring.
 *       However, it avoids crashing server when submitting a lot of jobs on the same server,
 *       by stopping the job manager within 9 to 10 seconds.
 * fixme: method cancel restarts the job, except for jobs submitted by command globusrun (with save_state=yes)
 */
public class UnmonitoredJobControlAdaptor extends GatekeeperJobAdaptorAbstract implements JobControlAdaptor, CleanableJobAdaptor {
    private static Logger s_logger = Logger.getLogger(UnmonitoredJobControlAdaptor.class);

    private UnmonitoredJobMonitorAdaptor m_monitor = new UnmonitoredJobMonitorAdaptor();

    public String getType() {
        return "unmonitored";
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return m_monitor;
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
    }

    public void disconnect() throws NoSuccessException {
        super.disconnect();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/rsl-1.0.xsl");
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId)  throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	RslNode rslTree;
        try {
        	rslTree = RSLParser.parse(jobDesc);
        } catch (ParseException e) {
            throw new NoSuccessException(e);
        }
        return submit(rslTree, checkMatch, false);
    }

    protected String submit(RslNode rslTree, boolean checkMatch, boolean isInteractive) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        if(checkMatch) {
			s_logger.debug("CheckMatch not supported");
		}
        GramJob job = new GramJob(m_credential, rslTree.toRSL(true));
        try {
        	try {
            	Gram.request(m_serverUrl, job, isInteractive);
        	} catch (WaitingForCommitException e) {
        		// send signal to start job
        		try{Thread.sleep(1000);} catch(InterruptedException e1){s_logger.warn(e1);}
        		job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
        	}
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException("Failed to submit job", e);
        }

        //NOTE: this prevents from crashing Gatekeeper but disables job monitoring
        try {
            job.signal(GRAMConstants.SIGNAL_STOP_MANAGER);
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
        return job.getIDAsString();
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = this.restart(nativeJobId);
        try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException("Failed to cancel job", e);
        }

        //todo: job.signal(GRAMConstants.SIGNAL_STOP_MANAGER) ???

        // tell job monitor to return status CANCELED
        m_monitor.cancel();
    }

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = super.getGramJobById(nativeJobId);
        try {
            // Send signal to clean jobmanager
            job.signal(GRAMConstants.SIGNAL_COMMIT_END);
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException("Failed to send commit end signal", e);
        }
	}

    private GramJob restart(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = new GramJob(m_credential, "&(restart="+nativeJobId+")(proxy_timeout=240)");
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e1) {
            throw new NoSuccessException(e1);
        }
        try {
            try {
                Gram.request(m_serverUrl, job);
            } catch (WaitingForCommitException e) {
                job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
            }
        } catch (GramException e) {
                this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException("Failed to restart jobmanager", e);
        }
        return job;
    }

    private void rethrowException(GramException e) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource{
    	switch(e.getErrorCode()) {
    		case GRAMProtocolErrorConstants.BAD_DIRECTORY:
    			throw new BadResource(e);
            case GRAMProtocolErrorConstants.ERROR_AUTHORIZATION:
                throw new PermissionDeniedException(e);
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new TimeoutException(e);
            default:
                throw new NoSuccessException(e);
        }
    }
}
