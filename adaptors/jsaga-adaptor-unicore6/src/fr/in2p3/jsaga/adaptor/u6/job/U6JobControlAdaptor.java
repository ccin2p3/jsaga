package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.PseudoInteractiveJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.ArgumentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.unigrids.x2006.x04.services.tss.TargetSystemPropertiesDocument;
import org.w3c.dom.Element;

import com.intel.gpe.client2.common.i18n.Messages;
import com.intel.gpe.client2.common.i18n.MessagesKeys;
import com.intel.gpe.client2.common.requests.PutFilesRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.transfers.FileExport;
import com.intel.gpe.client2.transfers.FileImport;
import com.intel.gpe.client2.transfers.TransferFailedException;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.JobType;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.GPEFileTransferProtocolNotSupportedException;
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
import java.io.InputStream;
import java.io.OutputStream;
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
public class U6JobControlAdaptor extends U6JobAdaptorAbstract 
		implements JobControlAdaptor, CleanableJobAdaptor, PseudoInteractiveJobAdaptor {

	protected static final String DEFAULT_CPU_TIME = "DefaultCpuTime";
	private int cpuTime;
	
    public Usage getUsage() {
    	return new UAnd(new Usage[]{
    			new U(APPLICATION_NAME),
    			new U(DEFAULT_CPU_TIME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return new Default[]{
    			new Default(APPLICATION_NAME, "Bash shell"),
    			new Default(DEFAULT_CPU_TIME, "3600")};
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
    
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	// get DEFAULT_CPU_TIME
    	try {
    		cpuTime = Integer.parseInt((String) attributes.get(DEFAULT_CPU_TIME));
    	}
    	catch(NumberFormatException e) {
    		throw new BadParameter("DefaultCpuTime value is not an integer",e);
		}
    }
    
    public JobClient createJob(GPEJobImpl jobJsdl , boolean checkMatch, String scriptFilename, TargetSystemInfo targetSystemInfo) 
    	throws PermissionDenied, Timeout, NoSuccess, BadResource {
    	try {
    		
	        // get target and create new job
	        JobType jobType = targetSystemInfo.getTargetSystem().getJobType(JobType.JobDefinitions.GPEJSDL);
	        GPEJob jobToSubmit = targetSystemInfo.getTargetSystem().newJob(jobType);	       
	        jobToSubmit.setApplicationName(targetSystemInfo.getApplicationName());
	        jobToSubmit.setApplicationVersion(targetSystemInfo.getApplicationVersion());
	        jobToSubmit.setId(String.valueOf(new Date().getTime()));
	        jobToSubmit.addField("SOURCE", scriptFilename);
	        
	        // get env
	        if(jobJsdl.getPOSIXApplication().getEnvironmentArray() != null) {
	        	EnvironmentType[] env = jobJsdl.getPOSIXApplication().getEnvironmentArray();
	        	for (int i = 0; i < env.length; i++) {
					jobToSubmit.addField(env[i].getName(), env[i].getStringValue());			        
				}
	        }
	        
	        // init termination time
	        boolean terminationTimeIsSet = false;
    		Calendar terminationTime = Calendar.getInstance();
    		
	        // verify prerequisite if specified
	        if(jobJsdl.getValue().getJobDescription().getResources() != null  && 
	        		(jobJsdl.getValue().getJobDescription().getResources().getCPUArchitecture() != null ||
	        		jobJsdl.getValue().getJobDescription().getResources().getTotalPhysicalMemory() != null || 
	        		jobJsdl.getValue().getJobDescription().getResources().getTotalCPUTime() != null || 
	        		jobJsdl.getValue().getJobDescription().getResources().getTotalCPUCount() != null )) {

		        TargetSystemPropertiesDocument tSP = 
		        	TargetSystemPropertiesDocument.Factory.parse((Element)targetSystemInfo.getTargetSystem().getResourcePropertyDocument());
	    		
	    		// verify CPU CPUArchitecture
		        if(jobJsdl.getValue().getJobDescription().getResources().getCPUArchitecture() != null) {
	    			if(!jobJsdl.getValue().getJobDescription().getResources().getCPUArchitecture().getCPUArchitectureName().toString().equals(tSP.getTargetSystemProperties().getProcessor().getCPUArchitecture().getCPUArchitectureName().toString()))
	    				throw new BadResource("CPU Architecture requirement is not supported.");
	    		}

	    		// verify Memory
	        	RangeValueType memoryRequested = jobJsdl.getValue().getJobDescription().getResources().getTotalPhysicalMemory();			
		        if(memoryRequested != null) {
	    			double upperLimit = tSP.getTargetSystemProperties().getIndividualPhysicalMemory().getRangeArray()[0].getUpperBound().getDoubleValue();
	    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualPhysicalMemory().getRangeArray()[0].getLowerBound().getDoubleValue();
	    			
	    			// get Exact value
	    			if(memoryRequested.getExactArray() !=  null &&  
	    					memoryRequested.getExactArray().length > 0 &&
	    					memoryRequested.getExactArray()[0].getDoubleValue() > 0) {
	    				double exact = memoryRequested.getExactArray()[0].getDoubleValue()*1024*1024;
		    			if(exact > upperLimit  || exact < lowerLimit ) {
		    				throw new BadResource("Memory requirement is not valid: "+ exact/1024/1024 + " is not in the range " +
		    						"between "+lowerLimit/1024/1024 + " and "+upperLimit/1024/1024+".");
	
		    			}
	    			}
	    			
	    			// get lower
	    			if(memoryRequested.getLowerBoundedRange() != null) {
		    			double lower = memoryRequested.getLowerBoundedRange().getDoubleValue()*1024*1024;
		    			if(lower > 0 ) {
		    				if(lower < lowerLimit) {
			    				throw new BadResource("Memory requirement is not valid: "+ lower/1024/1024 + " is not in the range " +
			    						"between "+lowerLimit/1024/1024 + " and "+upperLimit/1024/1024+".");
		
			    			}    				
		    			}
	    			}
	
	    			// get upper
	    			if(memoryRequested.getUpperBoundedRange() != null) {
		    			double upper = memoryRequested.getUpperBoundedRange().getDoubleValue()*1024*1024;
		    			if(upper > 0) {
		    				if(upper > upperLimit) {
			    				throw new BadResource("Memory requirement is not valid: "+ upper/1024/1024 + " is not in the range " +
			    						"between "+lowerLimit/1024/1024 + " and "+upperLimit/1024/1024+".");
		
			    			}
		    			}
	    			}
	    		}
	    		
	    		// verify and set CPU TIME
	    		RangeValueType cpuTimeRequested = jobJsdl.getValue().getJobDescription().getResources().getTotalCPUTime();
	    		if(cpuTimeRequested != null ) {
	    			
	    			double upperLimit = tSP.getTargetSystemProperties().getIndividualCPUTime().getRangeArray()[0].getUpperBound().getDoubleValue();
	    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualCPUTime().getRangeArray()[0].getLowerBound().getDoubleValue();
	    			
	    			// get Exact value
	    			if(cpuTimeRequested.getExactArray() != null && 
	    					cpuTimeRequested.getExactArray().length > 0 && 
	    					cpuTimeRequested.getExactArray()[0].getDoubleValue() > 0) {
	    				double requestCpuTimeInSeconds = cpuTimeRequested.getExactArray()[0].getDoubleValue();
		    			if(requestCpuTimeInSeconds > upperLimit || requestCpuTimeInSeconds < lowerLimit) {
		    				throw new BadResource("CPU Time requirement is not valid: "+requestCpuTimeInSeconds+ " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
		    			terminationTimeIsSet = true;
		    			terminationTime.add(Calendar.SECOND,(int) requestCpuTimeInSeconds);
	    			}
	    			
	    			// get lower
	    			if(cpuTimeRequested.getLowerBoundedRange()!= null &&
	    					cpuTimeRequested.getLowerBoundedRange().getDoubleValue() > 0 ) {
	    				if(cpuTimeRequested.getLowerBoundedRange().getDoubleValue() < lowerLimit) {
		    				throw new BadResource("CPU Time requirement is not valid: "+cpuTimeRequested.getLowerBoundedRange().getDoubleValue() + " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
	    			}
	
	    			// get upper
	    			if(cpuTimeRequested.getUpperBoundedRange()!= null &&
	    					cpuTimeRequested.getUpperBoundedRange().getDoubleValue() > 0 ) {
	    				if(cpuTimeRequested.getUpperBoundedRange().getDoubleValue() > upperLimit) {
		    				throw new BadResource("CPU Time requirement is not valid: "+cpuTimeRequested.getUpperBoundedRange().getDoubleValue() + " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
	    				terminationTimeIsSet = true;
	        			terminationTime.add(Calendar.SECOND,(int) cpuTimeRequested.getUpperBoundedRange().getDoubleValue());
	    			}
	    		}
	    		
	    		// verify CPU COUNT
	    		RangeValueType cpuCountRequested = jobJsdl.getValue().getJobDescription().getResources().getTotalCPUCount();
	    		if(cpuCountRequested != null ) {
	    			
	    			double upperLimit = tSP.getTargetSystemProperties().getIndividualCPUCount().getRangeArray()[0].getUpperBound().getDoubleValue();
	    			double lowerLimit = tSP.getTargetSystemProperties().getIndividualCPUCount().getRangeArray()[0].getLowerBound().getDoubleValue();
	    			
	    			// get Exact value
	    			if(cpuCountRequested.getExactArray() != null && 
	    					cpuCountRequested.getExactArray().length > 0 && 
	    					cpuCountRequested.getExactArray()[0].getDoubleValue() > 0) {
	    				double requestCpuCount = cpuCountRequested.getExactArray()[0].getDoubleValue();
		    			if(requestCpuCount > upperLimit || requestCpuCount < lowerLimit) {
		    				throw new BadResource("CPU Count requirement is not valid: "+requestCpuCount+ " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
	    			}
	    			
	    			// get lower
	    			if(cpuCountRequested.getLowerBoundedRange()!= null &&
	    					cpuCountRequested.getLowerBoundedRange().getDoubleValue() > 0 ) {
	    				if(cpuCountRequested.getLowerBoundedRange().getDoubleValue() < lowerLimit) {
		    				throw new BadResource("CPU Count requirement is not valid: "+cpuCountRequested.getLowerBoundedRange().getDoubleValue() + " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
	    			}
	
	    			// get upper
	    			if(cpuCountRequested.getUpperBoundedRange()!= null &&
	    					cpuCountRequested.getUpperBoundedRange().getDoubleValue() > 0 ) {
	    				if(cpuCountRequested.getUpperBoundedRange().getDoubleValue() > upperLimit) {
		    				throw new BadResource("CPU Count requirement is not valid: "+cpuCountRequested.getUpperBoundedRange().getDoubleValue() + " is not in the range " +
		    						"between "+lowerLimit + " and "+upperLimit+".");
		    			}
	    			}
	    		}
	        }
    		// set default
    		if(!terminationTimeIsSet)
    			terminationTime.add(Calendar.HOUR, cpuTime);    		
	        
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
	        
	        return jobClient;            
    	} catch (GPEWrongJobTypeException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			if(e instanceof BadResource) {
				throw new BadResource(e);
			}
			throw new NoSuccess(e);
		} catch (Throwable e) {
			throw new NoSuccess(e);
		}
    }
    
    public JobIOHandler submitInteractive(String jobDesc, boolean checkMatch, InputStream inputStream) 
    		throws PermissionDenied, Timeout, NoSuccess {	
		try {
			// create target
			TargetSystemInfo targetSystemInfo = findTargetSystem();
			JobClient jobClient;
			if(inputStream != null) {
				// parse JSDL to get execution file 
				GPEJobImpl jobJsdl = new GPEJobImpl();
				jobJsdl.setValue(JobDefinitionType.Factory.parse(jobDesc));				
				String exec = jobJsdl.getPOSIXApplication().getExecutable().getStringValue();

				String jobScriptFilename = "script.sh";
				
				// For Optimization : run the input script directly
				if((exec.equals("/bin/sh") || exec.equals("sh") ||
						exec.equals("/bin/bash") || exec.equals("bash")) && 
						( jobJsdl.getPOSIXApplication().getArgumentArray() == null || 
								jobJsdl.getPOSIXApplication().getArgumentArray().length == 0 ) &&
						(jobJsdl.getPOSIXApplication().getWorkingDirectory() == null ||
								jobJsdl.getPOSIXApplication().getWorkingDirectory().equals("$PWD"))) {
					jobClient = createJob(jobJsdl, checkMatch, jobScriptFilename, targetSystemInfo);			    	
				}
				else {
					// run the "command < stdin" into a script
					jobClient = createJob(jobDesc, jobScriptFilename,targetSystemInfo, checkMatch);
				}
				// upload inputStream
				FileProvider fileProvider = new FileProvider();
		        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);	        
	    		List<FileImport> putters = fileProvider.preparePutters(jobClient.getWorkingDirectory());
	        	int i;
				for (i = 0; i < putters.size(); i++) {
				    try {
				    	putters.get(i).putFile(targetSystemInfo.getSecurityManager(), inputStream, jobScriptFilename);
				     }
				     catch (GPEFileTransferProtocolNotSupportedException e) {
				         continue;
				     }
				     break;
				}
				if (i == putters.size()) {
				     throw new Exception(
				             Messages.getString(MessagesKeys.common_requests_PutFilesRequest_Cannot_put_file_to_remote_location__no_suitable_protocol_found));
				}
			}
			else {
				jobClient = createJob(jobDesc, null, targetSystemInfo, checkMatch);
			}
			jobClient.start();
			String jobId = ((AtomicJobClientImpl) jobClient).getId().toString();
			return new U6JobIOHandler(targetSystemInfo, jobClient,jobId);
		} catch (Exception e) {
			if(e instanceof BadResource) {
				throw new BadResource(e);
			}
			throw new NoSuccess(e);
		}
	}

    public String submit(String jobDesc, boolean checkMatch)  
    		throws PermissionDenied, Timeout, NoSuccess, BadResource {
    	try {
        	TargetSystemInfo targetSystemInfo = findTargetSystem();        
        	JobClient jobClient = createJob(jobDesc, null, targetSystemInfo, checkMatch);
    		//start job
			jobClient.start();
	    	return ((AtomicJobClientImpl) jobClient).getId().toString();
		} catch (Exception e) {
			if(e instanceof BadResource) {
				throw new BadResource(e);
			}
			throw new NoSuccess(e);
		}
    }
    
    public JobClient createJob(String jobDesc, String stdinScript, TargetSystemInfo targetSystemInfo, boolean checkMatch) 
    		throws PermissionDenied, Timeout, NoSuccess , BadResource {

    	try {
			// parse JSDL to create jobDefinition 
			GPEJobImpl jobJsdl = new GPEJobImpl();
			jobJsdl.setValue(JobDefinitionType.Factory.parse(jobDesc));
	
	    	// create input script file
			String commandLine = "JOBDIR=$PWD; ";
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
			if(stdinScript != null)
				commandLine += " < $JOBDIR/"+ stdinScript;
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
	    	
	    	// create job
			JobClient jobClient = createJob(jobJsdl, checkMatch, scriptFile.getName(), targetSystemInfo);
	        
	        // upload script file
	        StorageClient storage = jobClient.getWorkingDirectory();            
	        FileProvider fileProvider = new FileProvider();
	        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);
	        PutFilesRequest request = new PutFilesRequest(fileProvider, storage, inputFiles, targetSystemInfo.getSecurityManager());
	        request.perform();
	        
	        // clean script file
	        scriptFile.delete();
	        
	    	return jobClient;
    	} catch (GPESecurityException e) {
			throw new PermissionDenied(e);
		} catch (Throwable e) {
			if(e instanceof BadResource) {
				throw new BadResource(e);
			}
			throw new NoSuccess(e);
		}
	}
    
    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {    	
    	try {
    		// abort
    		getJobById(nativeJobId).abort();
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

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {
			// destroy
	        getJobById(nativeJobId).destroy();
		} catch (Exception e1) {
			throw new NoSuccess(e1);
		}
	}
	
}