package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.common.requests.PutFilesRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.JobType;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.GPEJobNotAbortedException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareRemoteException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareServiceException;
import com.intel.gpe.clients.api.exceptions.GPEResourceUnknownException;
import com.intel.gpe.clients.api.exceptions.GPESecurityException;
import com.intel.gpe.clients.api.exceptions.GPEWrongJobTypeException;
import com.intel.gpe.clients.api.jsdl.gpe.GPEJob;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.gridbeans.LocalGPEFile;
import com.intel.gpe.util.sets.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/
/**
 * TODO : support des pre-requis : utilisation direct du JSDL par Unicore
 * TODO : test_run_memoryRequirement à tester pour savoir si il faut garder le test
 */
public class U6JobControlAdaptor extends U6JobAdaptorAbstract implements JobControlAdaptor{

    public String getType() {
        return "unicore6";
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/jsdl.xsl";
    }

    public Map getTranslatorParameters() {
        return null;
    } 

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new U6JobMonitorAdaptor();
    }

    public String submit(String jobDesc) throws PermissionDenied, Timeout, NoSuccess {
    	try {                         
            // create job script with selected values
    		File scriptFile = File.createTempFile("script", ".sh");    		
    		PrintWriter fich = new PrintWriter(new FileWriter(scriptFile.getAbsolutePath()));
			fich.print(jobDesc);
			fich.close();
			
    		// register script as input file
			GPEFile gpeFile = new LocalGPEFile(scriptFile.getAbsolutePath());
			List<Pair<GPEFile, String>> inputFiles = new Vector<Pair<GPEFile, String>>();
	        inputFiles.add(new Pair<GPEFile, String>(gpeFile, scriptFile.getName()));
	        
	        // get target
	        m_securityManager = this.setSecurity(m_credential);
	        TargetSystemInfo targetSystemInfo = findTargetSystem();
	        
	        // Prepare the JSDL template
	        JobType jobType = targetSystemInfo.getTargetSystem().getJobType(JobType.JobDefinitions.GPEJSDL);
	        GPEJob job = targetSystemInfo.getTargetSystem().newJob(jobType);
	        // Fill in the JSDL template...
	        job.setApplicationName(targetSystemInfo.getApplicationName());
	        job.setApplicationVersion(targetSystemInfo.getApplicationVersion());
	        job.setId(String.valueOf(new Date().getTime()));
	        // Add an ARGUMENTS field for application parameters...
	        job.addField("ARGUMENTS", scriptFile.getName());
            
            // submit the job to target system and start it
	    	Calendar terminationTime = Calendar.getInstance();
	    	
	        // TODO, set 1 day ?
	        terminationTime.add(Calendar.HOUR, 3600);
	        
	        // submit job
	        JobClient jobClient = targetSystemInfo.getTargetSystem().submit(job, terminationTime);
	        
	        // Wait until the job is ready to start or failed
	        while (!jobClient.getStatus().isReady()
	                && !jobClient.getStatus().isFailed()) {
	        	Thread.sleep(100);
	        }

	        if (jobClient.getStatus().isFailed()) {
	            throw new NoSuccess("Unable to submit job: job already failed !");
	        }                
	        
	        // upload script file
	        StorageClient storage = jobClient.getWorkingDirectory();            
	        FileProvider fileProvider = new FileProvider();
	        fileProvider.addFileGetter("RBYTEIO", RandomByteIOFileExportImpl.class);
	        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);
	        PutFilesRequest request = new PutFilesRequest(fileProvider, storage, inputFiles, m_securityManager);
	        request.perform();
            // clean script file
            scriptFile.delete();
            
	        // start job
	        jobClient.start();
            return ((AtomicJobClientImpl) jobClient).getId().toString();            
    	} catch (GPEWrongJobTypeException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		} catch (Throwable e) {
			throw new NoSuccess(e);
		}
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {    	
    	try {
    		// set security
    		GPESecurityManager securityManager = this.setSecurity(m_credential);	        
    		// get Job
    		JobClient jobClient = getJobById(nativeJobId, securityManager);
    		// abort
    		jobClient.abort();
    		// get Outputs
    		getOutputs(jobClient, securityManager);
    	} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPESecurityException e) {
			throw new PermissionDenied(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (GPEJobNotAbortedException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
    }

	public boolean resume(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {
			// set security
    		GPESecurityManager securityManager = this.setSecurity(m_credential);	        
    		// get Job
    		JobClient jobClient = getJobById(nativeJobId,securityManager);
    		// resume
    		jobClient.resume();
    		return true;
    	} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPESecurityException e) {
			throw new PermissionDenied(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}    
    
}
