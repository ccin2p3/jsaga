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
    public LocalJobStatus(String jobId, int returnCode) {
        super(jobId, Integer.valueOf(returnCode), "unknown", returnCode);
    }

	public String getModel() {
        return "local";
    }

    public SubState getSubState() {
    	// select status with exitCode
        switch(((Integer)m_nativeStateCode).intValue()) {
            case 0:
                return SubState.DONE;
            case -1:
                return SubState.RUNNING_ACTIVE;
            default:
                return SubState.FAILED_ERROR;
        }
    }
}
