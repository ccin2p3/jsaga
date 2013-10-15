package fr.in2p3.jsaga.adaptor.dirac.job;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
* ***************************************************/

public class DiracJobStatus extends JobStatus {

	Logger m_logger = Logger.getLogger(this.getClass());
	private final static String DIRAC_STATUS_RECEIVED = "Received";
	private final static String DIRAC_STATUS_WAITING = "Waiting";
	private final static String DIRAC_STATUS_MATCHED = "Matched";
	private final static String DIRAC_STATUS_RUNNING = "Running";
	private final static String DIRAC_STATUS_FAILED = "Failed";
	private final static String DIRAC_STATUS_DELETED = "Deleted";
	private final static String DIRAC_STATUS_COMPLETED = "Completed";
	private final static String DIRAC_STATUS_DONE = "Done";

//	private final static String DIRAC_MINORSTATUS_EXECUTION_COMPLETE = "Execution Complete";
//	Waiting / "Pilot Agent Submission";
//	Deleted / Checking accounting
// Failed / Maximum of reschedulings reached
	
	public DiracJobStatus(JSONObject jobInfo) {
		super(((Long)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_JID)).toString(), 
				jobInfo, 
				(String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_STATUS));
	}
	
    public SubState getSubState() {
    	JSONObject jobInfo = (JSONObject)m_nativeStateCode;
    	String minorStatus = (String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_MINOR_STATUS);
    	String status = (String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_STATUS);
    	if (DIRAC_STATUS_DONE.equals(status)) {
    		if ("Execution Complete".equals(minorStatus)) 
    			return SubState.DONE;
    	} else if (DIRAC_STATUS_COMPLETED.equals(status)) {
    		return SubState.DONE;
    	} else if (DIRAC_STATUS_RECEIVED.equals(status)) {
    		return SubState.NEW_CREATED;
    	} else if (DIRAC_STATUS_WAITING.equals(status)) {
    		return SubState.RUNNING_QUEUED;
    	} else if (DIRAC_STATUS_MATCHED.equals(status)) {
    		return SubState.RUNNING_SUBMITTED;
    	} else if (DIRAC_STATUS_RUNNING.equals(status)) {
    		return SubState.RUNNING_ACTIVE;
        } else if (DIRAC_STATUS_DELETED.equals(status)) {
        	return SubState.CANCELED;
        } else if (DIRAC_STATUS_FAILED.equals(status)) {
        	return SubState.FAILED_ABORTED;
        } else {
        	m_logger.warn("Unknown status:" + status);
        }
    	return null;
    }

	@Override
	public String getModel() {
		return "dirac";
	}
}
