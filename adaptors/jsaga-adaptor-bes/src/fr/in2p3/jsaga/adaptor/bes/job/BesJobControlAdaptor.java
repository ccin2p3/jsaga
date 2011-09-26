package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
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
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobControlAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

/**
 * This class is the abstract class for the JobControl specific to a BES implementation
 */
public class BesJobControlAdaptor extends BesJobAdaptorAbstract implements JobControlAdaptor {

    protected static final String STAGING_DIRECTORY_TAGNAME = "StagingDirectory";
    protected static final String DATA_STAGING_TAGNAME = "DataStaging";
    protected static final String PRE_STAGING_TRANSFERS_TAGNAME = "PreStagingIn";
    protected static final String POST_STAGING_TRANSFERS_TAGNAME = "PostStagingOut";
    

    ////////////////////////////////////////////////////
    // Implementation of the JobControlAdaptor interface
    ////////////////////////////////////////////////////
	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new BesJobMonitorAdaptor();
	}

    protected String getJobDescriptionTranslatorFilename() throws NoSuccessException {
    	return "xsl/job/bes-jsdl.xsl";
    }
    
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	return new JobDescriptionTranslatorXSLT(getJobDescriptionTranslatorFilename());
    }


    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {

		Logger.getLogger(BesJobControlAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(jobDesc));

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
		Logger.getLogger(BesJobControlAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(createActivity));
		try {
			response = _bes_pt.createActivity(createActivity);
			Logger.getLogger(BesJobControlAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
		} catch (NotAcceptingNewActivitiesFaultType e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new PermissionDeniedException(e);
		} catch (InvalidRequestMessageFaultType e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		} catch (UnsupportedFeatureFaultType e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		} catch (NotAuthorizedFaultType e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new PermissionDeniedException(e);
		} catch (RemoteException e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		}
		return activityId2NativeId(response.getActivityIdentifier());
	}
		
    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		TerminateActivitiesType request = new TerminateActivitiesType();
		EndpointReferenceType[] refs = new EndpointReferenceType[1];
		refs[0] = nativeId2ActivityId(nativeJobId);
		request.setActivityIdentifier(refs);
		TerminateActivitiesResponseType response = null;
		try {
			response = _bes_pt.terminateActivities(request);
			TerminateActivityResponseType r = response.getResponse(0);
			Logger.getLogger(BesJobControlAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(r));
			if (!r.isCancelled()) throw new NoSuccessException("Unable to cancel job");
		} catch (InvalidRequestMessageFaultType e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			Logger.getLogger(BesJobControlAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		}
	}

    /**
     * Check required resources against available resources
     * 
     * @param required_resources
     * @throws BadResource if available resources do not match with required resources
     */
    protected void checkResources(Resources_Type required_resources) throws BadResource {
		if (required_resources == null) return;
		// checkMatch, check resources asked (jsdl_type.getJobDescription().getResources()) VS resources available
		if ( _br != null) {
			if (required_resources.getCPUArchitecture() != null 
					&& _br.getCPUArchitecture() != null
					&& ! required_resources.getCPUArchitecture().equals(_br.getCPUArchitecture())) {
				throw new BadResource("CPU Architecture not matching");
			}
		}
		// TODO: check following resources described in _br
		/*
		 * java.lang.String resourceName,
           org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystem_Type operatingSystem,
           org.ggf.schemas.jsdl.x2005.x11.jsdl.CPUArchitecture_Type CPUArchitecture,
           java.lang.Double CPUCount,
           java.lang.Double CPUSpeed,
           java.lang.Double physicalMemory,
           java.lang.Double virtualMemory,
 
		 */
    }
    
    /**
     * Convert a String into a JobDefinition_Type object
     * @param nativeJobDescription the String containing the job description
     * @return a JobDefinition_Type object 
     * @throws BadResource
     */
	protected JobDefinition_Type getJobDescriptionTypeFromString(String nativeJobDescription) throws BadResource {
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
	protected JobDefinition_Type getJobDescriptionTypeFromNativeId(String nativeJobId) throws NoSuccessException {
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
    	GetActivityDocumentsResponseType response = null;
    	try {
			GetActivityDocumentsType request = new GetActivityDocumentsType();
			EndpointReferenceType[] refs = new EndpointReferenceType[nativeJobIdArray.length];
			int i=0;
			for (String nativeJobId: nativeJobIdArray) {
				refs[i++] = nativeId2ActivityId(nativeJobId);
			}
			request.setActivityIdentifier(refs);
			Logger.getLogger(BesJobControlAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(request));
			response = _bes_pt.getActivityDocuments(request);
			Logger.getLogger(BesJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			return response.getResponse();
		} catch (InvalidRequestMessageFaultType e) {
			Logger.getLogger(BesJobMonitorAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			Logger.getLogger(BesJobMonitorAdaptor.class).error(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(response));
			throw new NoSuccessException(e);
		}
    	
    }

    ////////////////////////////////////////////////////
    // Protected methods
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
    protected String getStagingDirectory(JobDefinition_Type jsdl_type) {
		//MessageElement stagingDirectory = jsdl_type.getJobDescription().get_any()[STAGING_DIRECTORY];
    	for (MessageElement me: jsdl_type.getJobDescription().get_any()) {
    		if (STAGING_DIRECTORY_TAGNAME.equals(me.getName())) {
    			return me.getElementsByTagName("URI").item(0).getFirstChild().getNodeValue();
    		}
    	}
    	return "";
    }

    protected StagingTransfer[] getStagingTransfers(JobDefinition_Type jsdl_type, String PreOrPost) {
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

}
