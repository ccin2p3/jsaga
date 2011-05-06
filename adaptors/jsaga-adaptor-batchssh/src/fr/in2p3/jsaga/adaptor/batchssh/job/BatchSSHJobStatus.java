package fr.in2p3.jsaga.adaptor.batchssh.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* File:   BatchSSHJobStatus
* Author: Yassine BACHAR & Taha BENYEZZA
* Date:   12 Fevrier 2011
* ***************************************************/

public class BatchSSHJobStatus extends JobStatus {
   
	private int m_retCode;
	
    public BatchSSHJobStatus(String jobId, SubState status) {
        super(jobId,status, status.toString());
    }

    public BatchSSHJobStatus(String jobId, String state, int returnCode) {
    	super(jobId, state, state);
    	m_retCode = returnCode;
    }
    
    public BatchSSHJobStatus(String jobId, String state) {
    	super(jobId, state, state);
    }
    
    public String getModel() {
        return "pbs-ssh";
    }

    public SubState getSubState() {
    	if (m_nativeStateCode.equals("C") || m_nativeStateCode.equals("E")) {
    		if (m_retCode == 0) return SubState.DONE;
    		else if (m_retCode == 271) return SubState.CANCELED;
    		else return SubState.FAILED_ERROR;
    	} else if (m_nativeStateCode.equals("H")) {
            return SubState.SUSPENDED_ACTIVE;
    	} else if (m_nativeStateCode.equals("Q")) {
            return SubState.RUNNING_QUEUED;
    	} else if (m_nativeStateCode.equals("S") || m_nativeStateCode.equals("W")) {
            return SubState.SUSPENDED_QUEUED;
    	} else  { // "R"
            return SubState.RUNNING_ACTIVE;
    	} 
    }

}
