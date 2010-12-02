package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

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
import org.ogf.saga.error.*;

import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;
import org.xml.sax.InputSource;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.xml.namespace.QName;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/
public class BesJobControlAdaptor extends BesJobAdaptorAbstract 
		implements JobControlAdaptor {

    public Usage getUsage() {
    	return null;
    }
	
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesJobMonitorAdaptor();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorJSDL();
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId)
    		throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	// Check if BESFactory isAcceptingNewActivities
        /*GetFactoryAttributesDocumentResponseType r;
		try {
			r = _bes_pt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
		} catch (InvalidRequestMessageFaultType e1) {
			throw new NoSuccessException(e1);
		} catch (RemoteException e1) {
			throw new NoSuccessException(e1);
		}
        FactoryResourceAttributesDocumentType attr = r.getFactoryResourceAttributesDocument();
        if (! attr.isIsAcceptingNewActivities()) {
        	throw new PermissionDeniedException("Is not accepting new activities");
        }*/
    	

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
		EndpointReferenceType activityIdentifier = response.getActivityIdentifier();
		ReferenceParametersType rpt = activityIdentifier.getReferenceParameters();
		for (MessageElement me: rpt.get_any()) {
			if ("JobSessionDir".equals(me.getName())) {
				return me.getFirstChild().getNodeValue();
			}
		}
        return activityIdentifier.getAddress().get_value().toString();
			
    }
    
    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	TerminateActivitiesType request = new TerminateActivitiesType();
		request.setActivityIdentifier(new BesJob(nativeJobId).getReferenceEndpoints());
		try {
			TerminateActivitiesResponseType response = _bes_pt.terminateActivities(request);
			TerminateActivityResponseType r = response.getResponse(0);
			if (! r.isCancelled()) {
				throw new NoSuccessException("Unable to cancel job");
			}
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
    }

}