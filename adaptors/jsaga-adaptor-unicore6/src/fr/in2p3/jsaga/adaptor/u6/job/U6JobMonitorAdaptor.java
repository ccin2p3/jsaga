package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.Status;


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
 * TODO QueryFilteredJob when cleanup defined
 */
public class U6JobMonitorAdaptor extends U6JobAdaptorAbstract implements QueryIndividualJob {

    public String getType() {
        return "unicore6";
    }
    
    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {

    	try {
    		
    		// set security
    		GPESecurityManager securityManager = this.setSecurity(m_credential);    		
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
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			return new U6JobStatus(nativeJobId, jobStatus, jobStatus.toString());			
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
