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

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobControlAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LCGCEJobControlAdaptor extends GatekeeperJobAdaptorAbstract implements JobControlAdaptor, CleanableJobAdaptor {
    private Logger logger = Logger.getLogger(LCGCEJobControlAdaptor.class);
    private boolean twoPhaseUsed = false;

    public String getType() {
        return "lcgce";
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new LCGCEJobMonitorAdaptor();
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
			logger.debug("CheckMatch not supported");
		}
        GramJob job = new GramJob(m_credential,rslTree.toRSL(true));
        try {
        	try {
            	Gram.request(m_serverUrl, job, isInteractive);
            	logger.debug("'two_phase' attribute was ignored.");
        	}
        	catch (WaitingForCommitException e) {
        		// send signal to start job
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
        		twoPhaseUsed = true;
        	}
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
        return job.getIDAsString();
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = getGramJobById(nativeJobId);
        try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
    }

    /*public boolean suspend(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_SUSPEND);
    }

    public boolean resume(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_RESUME);
    }

    public boolean signal(String nativeJobId, int signal) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccessException(e);
        }
        int errorCode = -1;
        try {
            errorCode = job.signal(signal);
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
        return errorCode==0;
    }*/

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

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		try {
			if(twoPhaseUsed ) {
				GramJob job = getGramJobById(nativeJobId);
				// Send signal to clean jobmanager
				job.signal(GRAMConstants.SIGNAL_COMMIT_END);
			}
		} catch (GramException e) {
			throw new NoSuccessException("Unable to send commit end signal", e);
		} catch (GSSException e) {
			throw new NoSuccessException("Unable to send commit end signal", e);
		}
	}
}
