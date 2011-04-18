package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SHJobStatus
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/

public class LocalJobStatus extends JobStatus {
    public LocalJobStatus(String jobId, int processStatus) {
        super(jobId, Integer.valueOf(processStatus), "unknown", processStatus);
    }

	public String getModel() {
        return "local";
    }

    public SubState getSubState() {
    	// select status with exitCode
        switch(((Integer)m_nativeStateCode).intValue()) {
            case LocalJobProcess.PROCESS_DONE_OK:
                return SubState.DONE;
            case LocalJobProcess.PROCESS_RUNNING:
                return SubState.RUNNING_ACTIVE;
            case LocalJobProcess.PROCESS_SUSPENDED:
            	return SubState.SUSPENDED_ACTIVE;
            default:
                return SubState.FAILED_ERROR;
        }
    }
}
