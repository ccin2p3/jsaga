package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
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
* File:   GatekeeperJobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GatekeeperJobControlAdaptor extends GatekeeperJobAdaptorAbstract implements JobControlAdaptor {
    private static final String SHELLPATH = "ShellPath";
    private Map m_parameters;

    public String getType() {
        return "gatekeeper";
    }

    public Usage getUsage() {
        return new UOptional(SHELLPATH);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/rsl.xsl";
    }

    public Map getTranslatorParameters() {
        return m_parameters;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new GatekeeperJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        super.connect(userInfo, host, port, basePath, attributes);
        m_parameters = attributes;
    }

    public void disconnect() throws NoSuccess {
        super.disconnect();
        m_parameters = null;
    }

    public String submit(String jobDesc) throws PermissionDenied, Timeout, NoSuccess {
        RslNode rslTree;
        try {
        	rslTree = RSLParser.parse(jobDesc);
        } catch (ParseException e) {
            throw new NoSuccess(e);
        }
        GramJob job = new GramJob(m_credential, rslTree.toRSL(true));
        try {
        	try {
        		// boolean set if the job is not interactive
            	Gram.request(m_serverUrl, job, false);
        	}
        	catch (WaitingForCommitException e) {
        		// send signal to start job
        		job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
        	}
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        return job.getIDAsString();
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
        try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
    }

    /*public boolean suspend(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_SUSPEND);
    }

    public boolean resume(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_RESUME);
    }

    public boolean signal(String nativeJobId, int signal) throws PermissionDenied, Timeout, NoSuccess {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
        int errorCode = -1;
        try {
            errorCode = job.signal(signal);
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        return errorCode==0;
    }*/

    private void rethrowException(GramException e) throws PermissionDenied, Timeout, NoSuccess {
        switch(e.getErrorCode()) {
            case GRAMProtocolErrorConstants.ERROR_AUTHORIZATION:
                throw new PermissionDenied(e);
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new Timeout(e);
            default:
                throw new NoSuccess(e);
        }
    }
}
