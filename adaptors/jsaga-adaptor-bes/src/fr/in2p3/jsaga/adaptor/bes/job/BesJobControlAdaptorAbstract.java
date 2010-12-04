package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAcceptingNewActivitiesFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAuthorizedFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.UnsupportedFeatureFaultType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
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

import javax.xml.namespace.QName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public abstract class BesJobControlAdaptorAbstract extends BesJobAdaptorAbstract implements JobControlAdaptor {

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
		
		adt.setJobDefinition(jsdl_type);
		
		CreateActivityType createActivity = new CreateActivityType();
		createActivity.setActivityDocument(adt);
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
}
