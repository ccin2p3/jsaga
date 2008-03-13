package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.Status;
import com.intel.gpe.clients.api.exceptions.GPEInvalidResourcePropertyQNameException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareRemoteException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareServiceException;
import com.intel.gpe.clients.api.exceptions.GPEResourceNotDestroyedException;
import com.intel.gpe.clients.api.exceptions.GPEResourceUnknownException;
import com.intel.gpe.clients.api.exceptions.GPESecurityException;
import com.intel.gpe.clients.api.exceptions.GPEUnmarshallingException;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U6JobMonitorAdaptor extends U6JobAdaptorAbstract implements QueryIndividualJob {
	// TODO QueryFilteredJob
	
    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }    
    
    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {

    	try {
    		
    		// set security
    		GPESecurityManager securityManager = this.setSecurity();    		
	        JobClient jobClient = getJobById(nativeJobId, securityManager);
    		Status jobStatus = jobClient.getStatus();
			
			// TODO : move to cleanup step
			if(jobStatus.isFailed() || jobStatus.isSuccessful()) {
				//try to get stdout & stderr						
				try {
					getOutputs(jobClient, securityManager);
				}
				catch(NoSuccess e) {
					e.printStackTrace();
				}
				catch(AuthenticationFailed e) {
					e.printStackTrace();
				}

		        // destroy
				try {
					jobClient.destroy();
				} catch (GPEResourceUnknownException e1) {
					e1.printStackTrace();
				} catch (GPEResourceNotDestroyedException e1) {
					e1.printStackTrace();
				} catch (GPESecurityException e1) {
					e1.printStackTrace();
				} catch (GPEMiddlewareRemoteException e1) {
					e1.printStackTrace();
				} catch (GPEMiddlewareServiceException e1) {
					e1.printStackTrace();
				}
			}
			return new U6JobStatus(nativeJobId, jobStatus, jobStatus.toString());			
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
    }        

    // TODO : uncomment when clean up OK
	/*public JobStatus[] getFilteredStatus(String filter) throws Timeout, NoSuccess {
		try {
			
    		// list jobs
            List<JobClient> jobList = targetSystemInfo.getTargetSystem().getJobs();
            U6JobStatus[] jobListStatus = new U6JobStatus[jobList.size()];            
            for (Iterator iterator = jobList.iterator(); iterator.hasNext();) {
				JobClient jobClient = (JobClient) iterator.next();
				//cancel
				Status jobStatus = jobClient.getStatus();
				jobListStatus[0] = new U6JobStatus("??", jobStatus, jobStatus.toString());
			}

            return jobListStatus;
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
	}*/
    
}
