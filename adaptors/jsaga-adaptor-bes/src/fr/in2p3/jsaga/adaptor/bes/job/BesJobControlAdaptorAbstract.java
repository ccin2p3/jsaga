package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentsResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityDocumentsType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
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
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
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

public abstract class BesJobControlAdaptorAbstract extends BesJobAdaptorAbstract implements JobControlAdaptor, StagingJobAdaptorOnePhase, CleanableJobAdaptor {

    private static final int STAGING_DIRECTORY = 0;
    
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new BesJobDescriptionTranslatorJSDL();
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
		
		// is coded in transformation
		/*
        MessageElement msg = new MessageElement();
        msg.setName("StagingDirectory");
        msg.setPrefix("jsaga");
        //msg.setNamespaceURI("http://www.naregi.org/nrl/ws/addressing");
        msg.setValue("/tmp/" + uniqId);
		MessageElement[] msgs = new MessageElement[]{msg};
		jsdl_type.getJobDescription().set_any(msgs);
		*/
		
		if (checkMatch)
			checkResources(jsdl_type.getJobDescription().getResources());
		
		adt.setJobDefinition(jsdl_type);
		
		CreateActivityType createActivity = new CreateActivityType();
		createActivity.setActivityDocument(adt);
		StringWriter writer = new StringWriter();
		/*try {
			ObjectSerializer.serialize(writer, createActivity, 
					new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CreateActivityType"));
			System.out.println(writer);
		} catch (SerializationException e) {
			e.printStackTrace();
		}*/
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
			/*writer = new StringWriter();
			try {
				ObjectSerializer.serialize(writer, response, 
						new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CreateActivityResponseType"));
				System.out.println(writer);
			} catch (SerializationException e1) {
				e1.printStackTrace();
			}*/
			throw new NoSuccessException(e);
		}
		/*StringWriter writer = new StringWriter();
		try {
			ObjectSerializer.serialize(writer, response, 
					new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CreateActivityResponseType"));
			System.out.println(writer);
		} catch (SerializationException e) {
			e.printStackTrace();
		}*/
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
			/*StringWriter writer = new StringWriter();
			try {
				ObjectSerializer.serialize(writer, response, 
						new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "TerminateActivitiesResponseType"));
				System.out.println(writer);
			} catch (SerializationException e) {
				e.printStackTrace();
			}*/
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

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
	}

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

    private String getStagingDirectory(JobDefinition_Type jsdl_type) {
		MessageElement stagingDirectory = jsdl_type.getJobDescription().get_any()[STAGING_DIRECTORY];
		return stagingDirectory.getValue();
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		StagingTransfer[] st = new StagingTransfer[0];
    	// Get JobDefinition from String
		JobDefinition_Type jsdl_type = getJobDescriptionTypeFromString(nativeJobDescription);
        /*
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);*/
    	// TODO : extract STAGING_TRANSFERS from Description
    	return st;
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		StagingTransfer[] st = new StagingTransfer[0];
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
        /*
        MessageElement preStageIn = getExtensions(jobDesc)[PRE_STAGE_IN];
        return toStagingTransferArray(preStageIn);*/
    	// TODO : extract STAGING_TRANSFERS from Job Descr retrieved by the server
    	return st;
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		StagingTransfer[] st = new StagingTransfer[0];
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
        /*
        MessageElement postStageOut = getExtensions(jobDesc)[POST_STAGE_OUT];
        return toStagingTransferArray(postStageOut);*/
    	// TODO : extract TRANSFERS OUT from Job Descr retrieved by the server
    	return st;
    }

    public String activityId2NativeId(EndpointReferenceType epr) throws NoSuccessException {
		BesJob _job;
		try {
			_job = (BesJob) getJobClass().newInstance();
		} catch (InstantiationException e) {
			throw new NoSuccessException(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccessException(e);
		}
		_job.setActivityIdentifier(epr);
		return _job.getNativeJobID();    	
    }

    public EndpointReferenceType nativeId2ActivityId(String nativeId) throws NoSuccessException {
		BesJob _job;
		try {
			_job = (BesJob) getJobClass().newInstance();
		} catch (InstantiationException e) {
			throw new NoSuccessException(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccessException(e);
		}
		_job.setNativeJobId(nativeId);
		return _job.getActivityIdentifier();   	
    }
    
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
	
	private JobDefinition_Type getJobDescriptionTypeFromNativeId(String nativeJobId) throws NoSuccessException {
    	GetActivityDocumentResponseType[] response = getActivityDocuments(new String[]{nativeJobId});
    	return response[0].getJobDefinition();
	}

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
			/*StringWriter writer = new StringWriter();
			try {
				System.out.println("----> REQUEST");
				ObjectSerializer.serialize(writer, request, 
						new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityDocumentsType"));
				System.out.println(writer);
				System.out.println("----> RESPONSE");
				ObjectSerializer.serialize(writer, response, 
						new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityDocumentsResponseType"));
				System.out.println(writer);
			} catch (SerializationException e) {
				e.printStackTrace();
			}*/
			return response.getResponse();
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
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
