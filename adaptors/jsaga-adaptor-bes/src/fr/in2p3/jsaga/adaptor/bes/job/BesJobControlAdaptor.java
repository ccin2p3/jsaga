package fr.in2p3.jsaga.adaptor.bes.job;

/*import com.intel.gpe.client2.common.i18n.Messages;
import com.intel.gpe.client2.common.i18n.MessagesKeys;
import com.intel.gpe.client2.common.requests.PutFilesRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.transfers.FileImport;
import com.intel.gpe.clients.api.*;
import com.intel.gpe.clients.api.exceptions.*;
import com.intel.gpe.clients.api.jsdl.gpe.GPEJob;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;
import com.intel.gpe.clients.impl.jms.GPEJobImpl;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.gridbeans.LocalGPEFile;
import com.intel.gpe.util.sets.Pair;
*/
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.ArgumentType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.ogf.saga.error.*;
//import org.unigrids.x2006.x04.services.tss.TargetSystemPropertiesDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;
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
		implements JobControlAdaptor/*, CleanableJobAdaptor, StreamableJobBatch*/ {

	protected static final String DEFAULT_CPU_TIME = "DefaultCpuTime";
	private int cpuTime;
	
    public Usage getUsage() {
    	return new UAnd(new Usage[]{
    			new U(APPLICATION_NAME),
    			new U(DEFAULT_CPU_TIME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(APPLICATION_NAME, "Bash shell"),
    			new Default(DEFAULT_CPU_TIME, "3600")};
    }
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesJobMonitorAdaptor();
    }

    /*
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	// get DEFAULT_CPU_TIME
    	try {
    		cpuTime = Integer.parseInt((String) attributes.get(DEFAULT_CPU_TIME));
    	}
    	catch(NumberFormatException e) {
    		throw new BadParameterException("DefaultCpuTime value is not an integer",e);
		}
    }*/
    
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorJSDL();
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId)
    		throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
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