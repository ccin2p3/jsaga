package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.ogf.saga.error.*;

import org.xml.sax.InputSource;

import java.io.*;
import java.util.*;


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
    	// TODO: test isAcceptingNewActivities    	
    	try {
            CreateActivityResponseType response = null;
            ActivityDocumentType adt = new ActivityDocumentType();
            
            StringReader sr = new StringReader(jobDesc);
            JobDefinition_Type jsdl_type = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(sr), JobDefinition_Type.class);
            
            adt.setJobDefinition(jsdl_type);
            
            CreateActivityType createActivity = new CreateActivityType();
            createActivity.setActivityDocument(adt);
            response = _bes_pt.createActivity(createActivity);

            return response.getActivityIdentifier().getAddress().get_value().toString();
			
		} catch (Exception e) {
			if(e instanceof BadResource) {
				throw new BadResource(e);
			}
			throw new NoSuccessException(e);
		}
    }
    
    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        /*
    	try {
    		// abort
    		getJobById(nativeJobId).abort();
    	} catch (GPEResourceUnknownException e) {
			throw new NoSuccessException(e);
		} catch (GPESecurityException e) {
			throw new PermissionDeniedException(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccessException(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccessException(e);
		} catch (GPEJobNotAbortedException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}*/
    }

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
	    /*
		try {
			// destroy
	        getJobById(nativeJobId).destroy();
		} catch (Exception e1) {
			throw new NoSuccessException(e1);
		}
	*/
	}
}