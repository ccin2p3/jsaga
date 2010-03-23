package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.exec.client.GramJob;
import org.globus.exec.generated.ExtensionsType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.gram.GramException;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import javax.xml.namespace.QName;
import java.util.Iterator;
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
public class WSGramJobControlAdaptor extends WSGramJobAdaptorAbstract implements StagingJobAdaptorOnePhase, CleanableJobAdaptor {
    private static final int STAGE_DIRECTORY = 0;
    private static final int PRE_STAGE_IN = 1;
    private static final int POST_STAGE_OUT = 2;
    private static final int _NB_EXTENSIONS_ = 3;

    public String getTranslator() {
        return "xsl/job/rsl-2.0.xsl";
    }

    public Map getTranslatorParameters() {
        return null;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new WSGramJobMonitorAdaptor();
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	try {
    		// parse and create
			try {
				RSLHelper.readRSL(jobDesc);
			}
			catch (RSLParseException e) {
				throw new NoSuccessException(e);
			}
    		
			if(checkMatch) {
                //TODO: remove boolean checkMatch or add dependency on Log4j
//				logger.debug("CheckMatch not supported");
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
    		//must be true, else if false, start job status listening and gt4/etc files are needed in $HOME
    		boolean isNotInteractiveJob = true; 
    		
            java.net.URL factoryURL = ManagedJobFactoryClientHelper.getServiceURL(m_serverUrl).getURL();
            EndpointReferenceType factoryEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryURL, m_serverBatch);

            // submit job
            gramJob.submit(factoryEndpoint, isNotInteractiveJob);
        	return gramJob.getHandle();
        } catch (GramException e) {
            return this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        } catch (Exception e) {
        	 throw new NoSuccessException(e);
		}
    }

    public String getStagingBaseURL() {
        return "gsiftp://"+m_serverHost+":2811/tmp";
    }

    public String getStagingDirectory(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobDescriptionType jobDesc;
        try {
            jobDesc = RSLHelper.readRSL(nativeJobDescription);
        } catch(RSLParseException e){
            throw new NoSuccessException(e);
        }
        MessageElement stageDirectory = getExtensions(jobDesc)[STAGE_DIRECTORY];
        return stageDirectory.getValue();
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobDescriptionType jobDesc;
        try {
            jobDesc = RSLHelper.readRSL(nativeJobDescription);
        } catch(RSLParseException e){
            throw new NoSuccessException(e);
        }
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);
    }

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = super.getGramJobById(nativeJobId);
        JobDescriptionType jobDesc;
        try {
            jobDesc = job.getDescription();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        MessageElement stageDirectory = getExtensions(jobDesc)[STAGE_DIRECTORY];
        return stageDirectory.getValue();
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = super.getGramJobById(nativeJobId);
        JobDescriptionType jobDesc;
        try {
            jobDesc = job.getDescription();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        GramJob job = super.getGramJobById(nativeJobId);
        JobDescriptionType jobDesc;
        try {
            jobDesc = job.getDescription();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        MessageElement postStageOut = getExtensions(jobDesc)[POST_STAGE_OUT];
        return toStagingTransferArray(postStageOut);
    }

    public void cancel(String nativeJobHandle) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	GramJob job = super.getGramJobById(nativeJobHandle);        
    	try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        } catch (Exception e) {
       	 	throw new NoSuccessException(e);
		}
    }
    
    private String rethrowException(GramException e) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        switch(e.getErrorCode()) {
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
		GramJob job = super.getGramJobById(nativeJobId);        
    	try {
            job.destroy();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        } catch (Exception e) {
       	 	throw new NoSuccessException(e);
		}
	}

    private static MessageElement[] getExtensions(JobDescriptionType jobDesc) throws NoSuccessException {
        ExtensionsType ext = jobDesc.getExtensions();
        if (ext!=null && ext.get_any().length==_NB_EXTENSIONS_) {
            return ext.get_any();
        } else {
            throw new NoSuccessException("Failed to retrieve extensions");
        }
    }
    private static StagingTransfer[] toStagingTransferArray(MessageElement elem) {
        StagingTransfer[] transfers = new StagingTransfer[elem.getLength()];
        Iterator it = elem.getChildElements();
        for (int i=0; it.hasNext(); i++) {
            MessageElement child = (MessageElement) it.next();
            transfers[i] = new StagingTransfer(
                    getStringValue(child, "sourceUrl"),
                    getStringValue(child, "destinationUrl"),
                    getBooleanValue(child, "append"));
        }
        return transfers;
    }
    private static String getStringValue(MessageElement elem, String key) {
        MessageElement child = elem.getChildElement(new QName("", key));
        if (child != null) {
            try {
                return child.getValue();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }
        } else {
            return null;
        }
    }
    private static boolean getBooleanValue(MessageElement elem, String key) {
        String value = getStringValue(elem, key);
        if (value != null) {
            return "true".equalsIgnoreCase(value);
        } else {
            return false;
        }
    }
}
