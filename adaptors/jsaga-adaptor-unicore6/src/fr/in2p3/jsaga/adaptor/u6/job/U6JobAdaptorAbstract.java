package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
import fr.in2p3.jsaga.adaptor.u6.U6Abstract;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.common.requests.GetFilesRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.client2.transfers.FileExport;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.GPEInvalidResourcePropertyQNameException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareRemoteException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareServiceException;
import com.intel.gpe.clients.api.exceptions.GPEResourceNotDestroyedException;
import com.intel.gpe.clients.api.exceptions.GPEResourceUnknownException;
import com.intel.gpe.clients.api.exceptions.GPESecurityException;
import com.intel.gpe.clients.api.exceptions.GPEUnmarshallingException;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.gridbeans.LocalGPEFile;
import com.intel.gpe.util.sets.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/
/**
 *
 */
public abstract class U6JobAdaptorAbstract extends U6Abstract implements SagaSecureAdaptor {
	
	protected JKSSecurityAdaptor m_credential;
    protected String rootLogDir = System.getProperty("user.home") + File.separator;    
    
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{JKSSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
    	 m_credential = (JKSSecurityAdaptor) securityAdaptor;
    }

    public int getDefaultPort() {
        return 8080;
    }
    
    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return new Default[]{
    			new Default(APPLICATION_NAME, "Bash shell")};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	m_serverUrl = "https://"+host+":"+port+basePath;
    	
    	// get APPLICATION_NAME
    	if(attributes.containsKey(APPLICATION_NAME))
    		m_applicationName = (String) attributes.get(APPLICATION_NAME);
    }

    public void disconnect() throws NoSuccess {
        m_serverUrl = null;
        m_credential = null;
    }    

	protected JobClient getJobById(String nativeJobId, GPESecurityManager securityManager) throws NoSuccess {
		try {		
	    	// TODO Optimize this
	        // list jobs
			m_securityManager = this.setSecurity(m_credential);	        
        	TargetSystemInfo targetSystemInfo = findTargetSystem();
	        List<JobClient> jobList = targetSystemInfo.getTargetSystem().getJobs();
	        for (JobClient jobClient : jobList) {
	        	String currentJobId = ((AtomicJobClientImpl) jobClient).getId().toString();
	        	if(currentJobId.equals(nativeJobId)) {
					return jobClient;
				}
			}
		} catch (GPEInvalidResourcePropertyQNameException e) {
			throw new NoSuccess(e);
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPEUnmarshallingException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
        throw new NoSuccess("Unable to get job:"+nativeJobId);
	}
	

    protected void getOutputs(JobClient jobClient, GPESecurityManager securityManager) throws AuthenticationFailed, NoSuccess {
    	try {
    		String jobId = ((AtomicJobClientImpl) jobClient).getId().toString();
    		jobId = jobId.substring(jobId.indexOf("?res=")+5, jobId.length());
    		jobId = jobId.substring(0,jobId.indexOf("[{http"));
    		
    		String logDir = rootLogDir + jobId + File.separator;
            // get logs directory to put stdout and stderr job files
    		if(!new File(logDir).exists()) {
                new File(logDir).mkdirs();
            }
    		
    		logDir = new File(logDir).getAbsolutePath()+File.separator;
    		FileProvider fileProvider = new FileProvider();
	        fileProvider.addFileGetter("RBYTEIO", RandomByteIOFileExportImpl.class);
	        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);	        
    		StorageClient workingDirectory = jobClient.getWorkingDirectory();
            List<FileExport> getters = fileProvider.prepareGetters(workingDirectory);
           
            // standard out will be placed in the local working directory
            LocalGPEFile stdoutDest = new LocalGPEFile(logDir+"stdout.txt");
            
            // we know U/X will always place stdout in the job dir as "stdout".
            // we could also query this from the ExecutionJSDL RP...
            // create tranfer list
            List<Pair<GPEFile, String>> transfers = new ArrayList<Pair<GPEFile, String>>();
            // add stdout transfer
            transfers.add(new Pair<GPEFile, String>(stdoutDest, "stdout"));
            
            // same for stderr...
            LocalGPEFile stderrDest = new LocalGPEFile(logDir+"stderr.txt");
            transfers.add(new Pair<GPEFile, String>(stderrDest, "stderr"));
            
            // build and execute request
            GetFilesRequest request = new GetFilesRequest(fileProvider, workingDirectory, transfers, securityManager, null);
            request.perform(getters);            
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPEResourceNotDestroyedException e) {
			throw new NoSuccess(e);
		} catch (GPESecurityException e) {
			throw new AuthenticationFailed(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (InstantiationException e) {
			throw new NoSuccess(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccess(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
    }
 }
