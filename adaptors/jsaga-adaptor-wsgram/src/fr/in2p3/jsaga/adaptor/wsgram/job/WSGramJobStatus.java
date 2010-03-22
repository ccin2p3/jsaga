package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

import org.globus.exec.generated.StateEnumeration;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramrJobStatus
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WSGramJobStatus extends JobStatus {
    public WSGramJobStatus(String jobId, StateEnumeration state, String stateString) {
        super(jobId, state, stateString);
    }

    public WSGramJobStatus(String jobId, StateEnumeration state, String stateString, String cause) {
        super(jobId, state, stateString, cause);
    }
    
    public String getModel() {
        return "WS-GRAM";
    }

    public SubState getSubState() {
    	String jobState = ((StateEnumeration) m_nativeStateCode).getValue();
    	if(jobState.equals(StateEnumeration._Unsubmitted))
            return SubState.RUNNING_SUBMITTED;
    	else if(jobState.equals(StateEnumeration._Pending))
    		return SubState.RUNNING_QUEUED;
    	else if(jobState.equals(StateEnumeration._StageIn))
            return SubState.RUNNING_QUEUED;
		else if(jobState.equals(StateEnumeration._Active))
			return SubState.RUNNING_ACTIVE;
		else if(jobState.equals(StateEnumeration._StageOut))
            return SubState.RUNNING_ACTIVE;
    	// Clean up is between StageOut and DONE
		else if(jobState.equals(StateEnumeration._CleanUp))
            return SubState.RUNNING_ACTIVE;
		else if(jobState.equals(StateEnumeration._Suspended))
            return SubState.SUSPENDED_QUEUED;
		else if(jobState.equals(StateEnumeration._Done))
            return SubState.DONE;
		else if(jobState.equals(StateEnumeration._Failed))
            return SubState.FAILED_ERROR;
		else {
			System.err.println("Invalid state :"+jobState);
			return null;
		}
    }
}
