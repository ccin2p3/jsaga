package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.exec.client.GramJob;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.gram.GramException;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramJobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************/
/**
 * TODO : setProtection
 * TODO : during cleanup, remove proxy saved in server in $HOME/.globus/gram_proxy...
 */
public class WSGramJobControlAdaptor extends WSGramJobAdaptorAbstract implements JobControlAdaptor {
    private Map m_parameters;

    public String getType() {
        return "wsgram";
    }

    public Usage getUsage() {
        return null;
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
        return new WSGramJobMonitorAdaptor();
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
    	try {
    		// parse and create
    		if(true) {
    			try {
    				//System.out.println("Job desc:"+jobDesc);
    				RSLHelper.readRSL(jobDesc);
    			}
    			catch (RSLParseException e) {
    				throw new NoSuccess(e);    				
    			}
    		}
    		
    		// create job with the rsl XML jobDesc    		
    		GramJob gramJob = new GramJob(jobDesc);
    		
            // set security
    		gramJob.setCredentials(m_credential);
    		
    		// set protection
    		//gramJob.setMessageProtectionType(GSIConstants.ENCRYPTION);
    		//gramJob.setMessageProtectionType(GSIConstants.SIGNATURE);
    		
    		Authorization authorization = org.globus.wsrf.impl.security.authorization.HostAuthorization.getInstance();
    		gramJob.setAuthorization(authorization);
    		
        	// get factory
    		//must be true, else if false, start job status listening and gt4/etc files are neeeded in $HOME
    		boolean isNotInteractiveJob = true; 
    		
            java.net.URL factoryURL = ManagedJobFactoryClientHelper.getServiceURL(m_serverUrl).getURL();
            EndpointReferenceType factoryEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryURL, m_serverBatch);

            // submit job
            gramJob.submit(factoryEndpoint, isNotInteractiveJob, true, "uuid:" + UUIDGenFactory.getUUIDGen().nextUUID());
        	return gramJob.getHandle();
        } catch (GramException e) {
            //this.rethrowException(e);
            throw new NoSuccess(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        } catch (Exception e) {
        	 throw new NoSuccess(e);
		}
    }

    public void cancel(String nativeJobHandle) throws PermissionDenied, Timeout, NoSuccess {
    	GramJob job = super.getGramJobById(nativeJobHandle);        
    	try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        } catch (Exception e) {
       	 throw new NoSuccess(e);
		}
    }
    
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
