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
    public LocalJobStatus(String jobId, int status) {
        super(jobId, status, "unknown");
    }

	public String getModel() {
        return "TODO";
    }

    public SubState getSubState() {
    	// select status with exitCode
    	if((Integer) m_nativeStateCode == 0)
    		return SubState.DONE;
    	else if((Integer) m_nativeStateCode == -1)
    		return SubState.RUNNING_ACTIVE;
    	else
    		return SubState.FAILED_ERROR;
    }
}
