package fr.in2p3.jsaga.adaptor.unicore.job;


import org.unigrids.services.atomic.types.StatusType;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/

public class UnicoreJobStatus extends JobStatus {

	public UnicoreJobStatus(String jobId, Object stateCode, String stateString) {
		super(jobId, stateCode, stateString);
	}

	public UnicoreJobStatus(String jobId, Object stateCode, String stateString,	Integer returnCode) {
		super(jobId, stateCode, stateString, returnCode);
	}

	public String getModel() {
        return "UNICORE";
    }

	@Override
	public SubState getSubState() {
		StatusType.Enum state = (StatusType.Enum) m_nativeStateCode;
    	String substate = null;
        if (StatusType.QUEUED.equals(state)) {
       		return SubState.RUNNING_QUEUED;
        } else if (StatusType.READY.equals(state)) {
       		return SubState.RUNNING_SUBMITTED;
        } else if (StatusType.RUNNING.equals(state)) {
       		return SubState.RUNNING_ACTIVE;
        } else if (StatusType.SUCCESSFUL.equals(state)) {
            return SubState.DONE;
        } else if (StatusType.FAILED.equals(state)) {
            return SubState.FAILED_ERROR;
        } else {
            return null;
        }
    }

}