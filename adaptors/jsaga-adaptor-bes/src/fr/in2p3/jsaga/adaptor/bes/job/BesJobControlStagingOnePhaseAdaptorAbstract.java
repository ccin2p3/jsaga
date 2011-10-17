package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.bes.job.control.staging.BesStagingJobAdaptor;

import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobControlStagingOnePhaseAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   10 Jan 2010
* ***************************************************/

/**
 * This class is the abstract class for the JobControl with 1-phase data staging specific to a BES implementation
 */
public abstract class BesJobControlStagingOnePhaseAdaptorAbstract extends BesJobControlAdaptor implements BesStagingJobAdaptor {

    private static final String XSLTPARAM_PROTOCOL = "Protocol";
    private static final String XSLTPARAM_PORT = "Port";
    private static final String XSLTPARAM_PATH = "Path";
    
	protected URI _ds_url ;
    
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	// _ds_url is built by connect()
    	JobDescriptionTranslator translator =  super.getJobDescriptionTranslator();
    	translator.setAttribute(XSLTPARAM_PROTOCOL, _ds_url.getScheme());
    	translator.setAttribute(XSLTPARAM_PORT, String.valueOf(_ds_url.getPort()));
    	translator.setAttribute(XSLTPARAM_PATH, _ds_url.getPath());
    	return translator;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	try {
			_ds_url = getDataStagingUrl(host, port, basePath, attributes);
		} catch (URISyntaxException e) {
			throw new NoSuccessException(e);
		}
    	
	}

	public void disconnect() throws NoSuccessException {
		_ds_url = null;
        super.disconnect();
    }

    ////////////////////////////////////////////////////
	// Implementation of the StagingJobAdaptorOnePhase interface
    ////////////////////////////////////////////////////

	public String getStagingDirectory(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from String
		JobDefinition_Type jsdl_type = getJobDescriptionTypeFromString(nativeJobDescription);
		// Extract stagingDirectory
		return getStagingDirectory(jsdl_type);
    }

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
		// Extract stagingDirectory
		return getStagingDirectory(jsdl_type);
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	// Get JobDefinition from String
		JobDefinition_Type jsdl_type = getJobDescriptionTypeFromString(nativeJobDescription);
		// Extract PreStaging transfers
		return getStagingTransfers(jsdl_type, PRE_STAGING_TRANSFERS_TAGNAME);
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
		// Extract PreStaging transfers
		return getStagingTransfers(jsdl_type, PRE_STAGING_TRANSFERS_TAGNAME);
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
		// Extract PostStaging transfers
		return getStagingTransfers(jsdl_type, POST_STAGING_TRANSFERS_TAGNAME);
    }

    
    
    
    
}