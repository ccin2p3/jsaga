package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;

import org.ggf.schemas.jsdl.JobDefinition_Type;
import org.ggf.schemas.jsdl.spmd.SPMDApplication_Type;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.ArgumentType;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.unigrids.x2006.x04.services.tss.TargetSystemPropertiesDocument;
import org.w3c.dom.Element;

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
import com.intel.gpe.clients.impl.jms.GPEJobImpl;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.gridbeans.LocalGPEFile;
import com.intel.gpe.util.sets.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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

    public String submit(String jobDesc, boolean checkMatch) throws PermissionDenied, Timeout, NoSuccess {
    	try {

    		// parse JSDL to create Saga jobDefinition 
    		JobDefinition_Type jsagaJob =(JobDefinition_Type)  JobDefinition_Type.unmarshal(new StringReader(jobDesc));
    		    		 
    		// get job description element to create GPE Job
    		StringWriter outString = new StringWriter();
    		jsagaJob.getJobDescription().marshal(outString);
    		GPEJobImpl jobJsdl = new GPEJobImpl();
    		jobJsdl.setValue(JobDefinitionType.Factory.parse(outString.toString()));
    		outString.close();
	        
	        // Create job command line
    		String commandLine = "";
    		// add working directory if specified
    		if(jobJsdl.getPOSIXApplication().getWorkingDirectory() != null && 
    				jobJsdl.getPOSIXApplication().getWorkingDirectory().getStringValue() != null && 
    				!jobJsdl.getPOSIXApplication().getWorkingDirectory().getStringValue().equals("") ) {
    				commandLine = "if [[ !( -d " + jobJsdl.getPOSIXApplication().getWorkingDirectory().getStringValue()+") ]] ;" +
    						" then exit 1; fi;" +
    						" cd "+jobJsdl.getPOSIXApplication().getWorkingDirectory().getStringValue()+";";    			
    		}
    		// add executable
    		commandLine += jobJsdl.getPOSIXApplication().getExecutable().getStringValue(); 
    		// add arguments
    		if(jobJsdl.getPOSIXApplication().getArgumentArray() != null) {
    			ArgumentType[] args = jobJsdl.getPOSIXApplication().getArgumentArray();
        		for (int i = 0; i < args.length; i++) {
        			commandLine += " " + args[i].getStringValue();
				}
    		}
    		commandLine += ";";
    		
    		// create job script with command line
    		File scriptFile = File.createTempFile("script", ".sh");
    		scriptFile.deleteOnExit();
    		PrintWriter fich = new PrintWriter(new FileWriter(scriptFile.getAbsolutePath()));
			fich.print(commandLine);
			fich.close();
			
    		// register job script as input file
			GPEFile gpeFile = new LocalGPEFile(scriptFile.getAbsolutePath());
			List<Pair<GPEFile, String>> inputFiles = new Vector<Pair<GPEFile, String>>();
	        inputFiles.add(new Pair<GPEFile, String>(gpeFile, scriptFile.getName()));     		
	        
	        // get target and create new job
    		m_securityManager = this.setSecurity(m_credential);
	        TargetSystemInfo targetSystemInfo = findTargetSystem();
	        JobType jobType = targetSystemInfo.getTargetSystem().getJobType(JobType.JobDefinitions.GPEJSDL);
	        GPEJob jobToSubmit = targetSystemInfo.getTargetSystem().newJob(jobType);	       
	        jobToSubmit.setApplicationName(targetSystemInfo.getApplicationName());
	        jobToSubmit.setApplicationVersion(targetSystemInfo.getApplicationVersion());
	        jobToSubmit.setId(String.valueOf(new Date().getTime()));
	        jobToSubmit.addField("SOURCE", scriptFile.getName());
	        TargetSystemPropertiesDocument tSP = 
	        	TargetSystemPropertiesDocument.Factory.parse((Element)targetSystemInfo.getTargetSystem().getResourcePropertyDocument());
    		
    		// verify CPU CPUArchitecture
	        if(jobJsdl.getCPUArchitectureRequirements() != null) {
    			if(!jobJsdl.getCPUArchitectureRequirements().name().equals(tSP.getTargetSystemProperties().getProcessor().getCPUArchitecture().getCPUArchitectureName().toString()))
    				throw new NoSuccess("CPU Architecture requirement is not supported.");
    		}
	        
    		// verify Memory
    		if(jsagaJob.getJobDescription().getResources() != null && 
    				jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory() != null) {
    			
    			double upperLimit = tSP.getTargetSystemProperties().getIndividualPhysicalMemory().getRangeArray()[0].getUpperBound().getDoubleValue();
    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualPhysicalMemory().getRangeArray()[0].getLowerBound().getDoubleValue();
    			
    			// get Exact value
    			if(jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getExact() != null && 
    					jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getExact().length > 0 ) {
    				double requestMemory = jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getExact()[0].getContent()*1024*1024;
	    			if(requestMemory > upperLimit  || requestMemory < lowerLimit ) {
	    				throw new NoSuccess("Memory requirement is not valid: "+ requestMemory*1024*1024 + " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");

	    			}
    			}
    			
    			// get lower
    			if(jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getLowerBoundedRange() != null ) {
    				if(jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getLowerBoundedRange().getContent()*1024*1024 
    						< lowerLimit) {
	    				throw new NoSuccess("Memory requirement is not valid: "+ jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getLowerBoundedRange().getContent()*1024*1024 + " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");

	    			}    				
    			}

    			// get upper
    			if(jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getUpperBoundedRange() != null) {
    				if(jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getUpperBoundedRange().getContent()*1024*1024 
    						> upperLimit) {
	    				throw new NoSuccess("Memory requirement is not valid: "+ jsagaJob.getJobDescription().getResources().getTotalPhysicalMemory().getUpperBoundedRange().getContent()*1024*1024 + " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");

	    			}
    			}
    		}
    		
    		// verify and set CPU TIME
    		Calendar terminationTime = Calendar.getInstance();
    		boolean terminationTimeIsSet = false;
    		if(jsagaJob.getJobDescription().getResources() != null && 
    				jsagaJob.getJobDescription().getResources().getTotalCPUTime() != null) {
    			
    			double upperLimit = tSP.getTargetSystemProperties().getIndividualCPUTime().getRangeArray()[0].getUpperBound().getDoubleValue();
    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualCPUTime().getRangeArray()[0].getLowerBound().getDoubleValue();
    			
    			// get Exact value
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUTime().getExact() != null && 
    					jsagaJob.getJobDescription().getResources().getTotalCPUTime().getExact().length > 0 ) {
    				double requestCpuTimeInSeconds = jsagaJob.getJobDescription().getResources().getTotalCPUTime().getExact()[0].getContent();
	    			if(requestCpuTimeInSeconds > upperLimit || requestCpuTimeInSeconds < lowerLimit) {
	    				throw new NoSuccess("CPU Time requirement is not valid: "+requestCpuTimeInSeconds+ " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
	    			terminationTimeIsSet = true;
	    			terminationTime.add(Calendar.SECOND,(int) requestCpuTimeInSeconds);
    			}
    			
    			// get lower
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUTime().getLowerBoundedRange() != null ) {
    				if(jsagaJob.getJobDescription().getResources().getTotalCPUTime().getLowerBoundedRange().getContent() 
    						< lowerLimit) {
	    				throw new NoSuccess("CPU Time requirement is not valid: "+jsagaJob.getJobDescription().getResources().getTotalCPUTime().getLowerBoundedRange().getContent() + " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
    			}

    			// get upper
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUTime().getUpperBoundedRange() != null) {
					if(jsagaJob.getJobDescription().getResources().getTotalCPUTime().getUpperBoundedRange().getContent() 
    						> upperLimit) {
	    				throw new NoSuccess("CPU Time requirement is not valid: "+jsagaJob.getJobDescription().getResources().getTotalCPUTime().getUpperBoundedRange().getContent()+ " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
    				terminationTimeIsSet = true;
        			terminationTime.add(Calendar.SECOND,(int) jsagaJob.getJobDescription().getResources().getTotalCPUTime().getUpperBoundedRange().getContent());
    			}
    		}    		    	
    		
    		// TODO : set default
    		if(!terminationTimeIsSet)
    			terminationTime.add(Calendar.HOUR, 3600);

    		// verify and set TOTAL CPU Process Per Node
    		/*TODO if(jobDesc.indexOf("<spmd:SPMDApplication") > 0 ) {
    			System.out.println("test:"+jobDesc);
    			String SPMDApplication = jobDesc.substring(jobDesc.indexOf("<spmd:SPMDApplication"), jobDesc.indexOf("</spmd:SPMDApplication>")+"</spmd:SPMDApplication>".length());
    			SPMDApplication_Type appli = (SPMDApplication_Type) SPMDApplication_Type.unmarshal(new StringReader(SPMDApplication));
    			System.out.println("Type:"+appli.getSPMDVariation());
    			double upperLimit = tSP.getTargetSystemProperties().getIndividualCPUCount().getRangeArray()[0].getUpperBound().getDoubleValue();
    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualCPUCount().getRangeArray()[0].getLowerBound().getDoubleValue();
    		} */
    		
    		// verify and set TOTAL CPU TIME
    		if(jsagaJob.getJobDescription().getResources() != null && 
    				jsagaJob.getJobDescription().getResources().getTotalCPUCount() != null) {
    			
    			double upperLimit = tSP.getTargetSystemProperties().getTotalResourceCount().getRangeArray()[0].getUpperBound().getDoubleValue();
    			double lowerLimit = tSP.getTargetSystemProperties().getTotalResourceCount().getRangeArray()[0].getLowerBound().getDoubleValue();
    			
    			// get Exact value
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUCount().getExact() != null && 
    					jsagaJob.getJobDescription().getResources().getTotalCPUCount().getExact().length > 0 ) {
    				double requestCpuTimeInSeconds = jsagaJob.getJobDescription().getResources().getTotalCPUCount().getExact()[0].getContent();
	    			if(requestCpuTimeInSeconds > upperLimit || requestCpuTimeInSeconds < lowerLimit) {
	    				throw new NoSuccess("Total CPU Count requirement is not valid: "+requestCpuTimeInSeconds+ " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
    			}
    			
    			// get lower
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUCount().getLowerBoundedRange() != null ) {
    				if(jsagaJob.getJobDescription().getResources().getTotalCPUCount().getLowerBoundedRange().getContent() 
    						< lowerLimit) {
	    				throw new NoSuccess("Total CPU Count requirement is not valid: "+jsagaJob.getJobDescription().getResources().getTotalCPUCount().getLowerBoundedRange().getContent() + " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
    			}

    			// get upper
    			if(jsagaJob.getJobDescription().getResources().getTotalCPUCount().getUpperBoundedRange() != null) {
    				if(jsagaJob.getJobDescription().getResources().getTotalCPUCount().getUpperBoundedRange().getContent() 
    						> upperLimit) {
	    				throw new NoSuccess("Total CPU Count requirement is not valid: "+jsagaJob.getJobDescription().getResources().getTotalCPUCount().getUpperBoundedRange().getContent()+ " is not in the range " +
	    						"between "+lowerLimit + " and "+upperLimit+".");
	    			}
    			}
    		}
	        
	        // submit job
	        JobClient jobClient = targetSystemInfo.getTargetSystem().submit(jobToSubmit, terminationTime);
	        
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
    
}
