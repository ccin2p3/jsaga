package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;


import com.intel.gpe.clients.api.Status;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobStatus
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U6JobStatus extends JobStatus {
    public U6JobStatus(String jobId, Status status, String stateString) {
        super(jobId, status, stateString);
    }

    public U6JobStatus(String jobId, Status state, String stateString, String cause) {
        super(jobId, state, stateString, cause);
    }    

	public String getModel() {
        return "TODO";
    }

    public SubState getSubState() {
    	if(m_nativeStateCode instanceof Status) {
	    	Status jobState = ((Status) m_nativeStateCode);
	    	if(jobState.isFailed())
	            return SubState.FAILED_ERROR;
	    	else if(jobState.isQueued())
	    		return SubState.RUNNING_QUEUED;
	    	else if(jobState.isReady())
	            return SubState.RUNNING_SUBMITTED;
			else if(jobState.isRunning())
				return SubState.RUNNING_ACTIVE;
			else if(jobState.isStagingIn())
	            return SubState.RUNNING_QUEUED;
			else if(jobState.isStagingOut())
	            return SubState.RUNNING_ACTIVE;
			else if(jobState.isSuccessful()) {
				String statusString = jobState.toString();			
				String exitCode = statusString.substring(statusString.indexOf("<typ:ExitCode>")+"</typ:ExitCode>".length()-1, statusString.indexOf("</typ:ExitCode>"));
				if(!exitCode.equals("0")) {
					return SubState.FAILED_ERROR;
				}
				else {
		            return SubState.DONE;
				}
			}
			else {
				if(jobState.isUndefined())
					System.err.println("Invalid state: undefined");
				else 
					System.err.println("Invalid state!");
				return null;
			}
    	}
    	else {
    		System.err.println("Invalid status instance:"+m_nativeStateCode.getClass().getName());
    		return null;
    	}
    }
}
