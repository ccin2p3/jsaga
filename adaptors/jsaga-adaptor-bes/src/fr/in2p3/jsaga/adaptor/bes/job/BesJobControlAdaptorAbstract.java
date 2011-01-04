package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.bes.BesUtils;
import fr.in2p3.jsaga.adaptor.bes.job.control.staging.BesStagingJobAdaptor;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentsResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentsType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAcceptingNewActivitiesFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAuthorizedFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.UnsupportedFeatureFaultType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.Resources_Type;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

/**
 * This class is the abstract class for the JobControl specific to a BES implementation
 */
public abstract class BesJobControlAdaptorAbstract extends BesJobAdaptorAbstract implements JobControlAdaptor, BesStagingJobAdaptor, CleanableJobAdaptor {

    private static final String STAGING_DIRECTORY_TAGNAME = "StagingDirectory";
    private static final String DATA_STAGING_TAGNAME = "DataStaging";
    private static final String PRE_STAGING_TRANSFERS_TAGNAME = "PreStagingIn";
    private static final String POST_STAGING_TRANSFERS_TAGNAME = "PostStagingOut";
    
    private static final String XSLTPARAM_PROTOCOL = "Protocol";
    private static final String XSLTPARAM_PORT = "Port";
    
    ////////////////////////////////////////////////////
    // Implementation of the JobControlAdaptor interface
    ////////////////////////////////////////////////////
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	JobDescriptionTranslator translator =  new JobDescriptionTranslatorXSLT("xsl/job/bes-jsdl.xsl");//BesJobDescriptionTranslatorJSDL();
    	translator.setAttribute(XSLTPARAM_PROTOCOL, getDataStagingProtocol());
    	translator.setAttribute(XSLTPARAM_PORT, String.valueOf(getDataStagingPort()));
    	return translator;
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {

		CreateActivityResponseType response = null;
		ActivityDocumentType adt = new ActivityDocumentType();
		
		StringReader sr = new StringReader(jobDesc);
		JobDefinition_Type jsdl_type;
		try {
			jsdl_type = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(sr), JobDefinition_Type.class);
		} catch (DeserializationException e) {
			throw new BadResource(e);
		}
		
		if (checkMatch)
			checkResources(jsdl_type.getJobDescription().getResources());
		
		adt.setJobDefinition(jsdl_type);
		
		CreateActivityType createActivity = new CreateActivityType();
		createActivity.setActivityDocument(adt);
		//System.out.println(BesUtils.dumpBESMessage(createActivity));
		try {
			response = _bes_pt.createActivity(createActivity);
		} catch (NotAcceptingNewActivitiesFaultType e) {
			throw new PermissionDeniedException(e);
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (UnsupportedFeatureFaultType e) {
			throw new NoSuccessException(e);
		} catch (NotAuthorizedFaultType e) {
			throw new PermissionDeniedException(e);
		} catch (RemoteException e) {
			//System.out.println(BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		}
		return activityId2NativeId(response.getActivityIdentifier());
	}
		
    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		TerminateActivitiesType request = new TerminateActivitiesType();
		EndpointReferenceType[] refs = new EndpointReferenceType[1];
		refs[0] = nativeId2ActivityId(nativeJobId);
		request.setActivityIdentifier(refs);
		try {
			TerminateActivitiesResponseType response = _bes_pt.terminateActivities(request);
			TerminateActivityResponseType r = response.getResponse(0);
			//System.out.println(BesUtils.dumpBESMessage(response));
			for (MessageElement me: r.get_any()) {
				if ("Terminated".equals(me.getName())) {
					if (! me.getFirstChild().getNodeValue().equals("true")) {
						throw new NoSuccessException("Unable to cancel job");
					}
				}
			}
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
	}

    ////////////////////////////////////////////////////
    // Implementation of the CleanableJobAdaptor interface
    ////////////////////////////////////////////////////

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
	}

    ////////////////////////////////////////////////////
	// Implementation of the StagingJobAdaptorOnePhase interface
    ////////////////////////////////////////////////////

	public String getStagingDirectory(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		System.out.println(nativeJobDescription);
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
        /*
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);*/
		return getStagingTransfers(jsdl_type, PRE_STAGING_TRANSFERS_TAGNAME);
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
        /*
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);*/
		return getStagingTransfers(jsdl_type, PRE_STAGING_TRANSFERS_TAGNAME);
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
        /*
        MessageElement postStageOut = getExtensions(jobDesc)[POST_STAGE_OUT];
        return toStagingTransferArray(postStageOut);*/
		return getStagingTransfers(jsdl_type, POST_STAGING_TRANSFERS_TAGNAME);
    }

    
    
    
    
    ////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////
    
    /**
     * Extract the StagingDirectory from the JSDL as follows:
     * 
     * <jsdl:JobDescription>
     *   <jsdl:JobIdentification/>
     *   <jsdl:Application>
     *     ...
     *   <jsaga:StagingDirectory><jsaga:URI>gsiftp://host:2811/tmp/1998763545</jsaga:URI></jsaga:StagingDirectory>	
     * </jsdl:JobDescription>
     * 
     * @param jsdl_type the JSDL Job Definition
     * @return the StagingDirectory defined in the JobDescription
     */
    private String getStagingDirectory(JobDefinition_Type jsdl_type) {
		//MessageElement stagingDirectory = jsdl_type.getJobDescription().get_any()[STAGING_DIRECTORY];
    	for (MessageElement me: jsdl_type.getJobDescription().get_any()) {
    		if (STAGING_DIRECTORY_TAGNAME.equals(me.getName())) {
    			return me.getElementsByTagName("URI").item(0).getFirstChild().getNodeValue();
    		}
    	}
    	return "";
    }

    private StagingTransfer[] getStagingTransfers(JobDefinition_Type jsdl_type, String PreOrPost) {
    	StagingTransfer[] st = new StagingTransfer[]{};
    	ArrayList transfers = new ArrayList();
    	for (MessageElement me: jsdl_type.getJobDescription().get_any()) {
    		if (DATA_STAGING_TAGNAME.equals(me.getName())) {
    			if (PreOrPost.equals(me.getFirstChild().getLocalName())) {
    				String from = me.getElementsByTagName("Source").item(0).getFirstChild().getFirstChild().getNodeValue();
    				String to = me.getElementsByTagName("Target").item(0).getFirstChild().getFirstChild().getNodeValue();
    				transfers.add(new StagingTransfer(from, to, false));
    			}
    		}
    	}
    	return (StagingTransfer[]) transfers.toArray(st);
    }

    /**
     * Check required resources against available resources
     * 
     * @param required_resources
     * @throws BadResource if available resources do not match with required resources
     */
    protected void checkResources(Resources_Type required_resources) throws BadResource {
		if (required_resources == null) return;
		// Check if TotalCPU Time required is too large > 5 days
		// TODO how to get a variable duration ?
		if (required_resources.getTotalCPUTime() != null 
				&& required_resources.getTotalCPUTime().getUpperBoundedRange() != null 
				&& required_resources.getTotalCPUTime().getUpperBoundedRange().get_value() > 60*60*24*5) {
			throw new BadResource("Total CPU Time is too large");
		}
		// checkMatch, check resources asked (jsdl_type.getJobDescription().getResources()) VS resources available
		if ( _br != null) {
			if (required_resources.getCPUArchitecture() != null 
					&& _br.getCPUArchitecture() != null
					&& ! required_resources.getCPUArchitecture().equals(_br.getCPUArchitecture())) {
				throw new BadResource("CPU Architecture not matching");
			}
		}
		// TODO: check resources
    }
    
    /**
     * Convert a String into a JobDefinition_Type object
     * @param nativeJobDescription the String containing the job description
     * @return a JobDefinition_Type object 
     * @throws BadResource
     */
	private JobDefinition_Type getJobDescriptionTypeFromString(String nativeJobDescription) throws BadResource {
    	StringReader sr = new StringReader(nativeJobDescription);
		JobDefinition_Type jsdl_type;
		try {
			jsdl_type = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(sr), JobDefinition_Type.class);
		} catch (DeserializationException e) {
			throw new BadResource(e);
		}
		return jsdl_type;
	}
	
	/**
	 * Get a JobDefinition_Type object of a job defined by its nativeID (BES GetActivityDocumentsType request)
	 * @param nativeJobId the native ID of the Job
	 * @return a JobDefinition_Type object 
	 * @throws NoSuccessException
	 */
	private JobDefinition_Type getJobDescriptionTypeFromNativeId(String nativeJobId) throws NoSuccessException {
    	GetActivityDocumentResponseType[] response = getActivityDocuments(new String[]{nativeJobId});
    	return response[0].getJobDefinition();
	}

	/**
	 * Get a list of job descriptions via BES
	 * 
	 * Send a GetActivityDocumentsType request containing the list of jobs
	 * and receives the list of description in GetActivityDocumentsResponseType
	 * @param nativeJobIdArray an array of native jobs Identifiers
	 * @return an array of GetActivityDocumentResponseType
	 * @throws NoSuccessException
	 */
    private GetActivityDocumentResponseType[] getActivityDocuments(String[] nativeJobIdArray) throws NoSuccessException{
		try {
			GetActivityDocumentsType request = new GetActivityDocumentsType();
			EndpointReferenceType[] refs = new EndpointReferenceType[nativeJobIdArray.length];
			int i=0;
			for (String nativeJobId: nativeJobIdArray) {
				refs[i++] = nativeId2ActivityId(nativeJobId);
			}
			request.setActivityIdentifier(refs);
			GetActivityDocumentsResponseType response = _bes_pt.getActivityDocuments(request);
			//System.out.println(BesUtils.dumpBESMessage(request));
			//System.out.println(BesUtils.dumpBESMessage(response));
			return response.getResponse();
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
    	
    }
}