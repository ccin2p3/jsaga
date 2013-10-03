package fr.in2p3.jsaga.adaptor.dirac.job;

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

	private final static String DIRAC_STATUS_DONE = "Done";
	private final static String DIRAC_STATUS_WAITING = "Waiting";
	private final static String DIRAC_STATUS_DELETED = "Deleted";

//	private final static String DIRAC_MINORSTATUS_EXECUTION_COMPLETE = "Execution Complete";
//	Waiting / "Pilot Agent Submission";
//	Deleted / Checking accounting
	
	public DiracJobStatus(String jobId, JSONObject jobInfo) {
		super(jobId, jobInfo, (String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_STATUS));
	}
	
    public SubState getSubState() {
    	// TODO getSubState
    	JSONObject jobInfo = (JSONObject)m_nativeStateCode;
    	String minorStatus = (String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_MINOR_STATUS);
    	String status = (String)jobInfo.get(DiracConstants.DIRAC_GET_RETURN_STATUS);
    	if (DIRAC_STATUS_DONE.equals(status)) {
    		if ("Execution Complete".equals(minorStatus)) 
    			return SubState.DONE;
    	} else if (DIRAC_STATUS_WAITING.equals(status)) {
    		return SubState.RUNNING_SUBMITTED;
        } else if (DIRAC_STATUS_DELETED.equals(status)) {
        	return SubState.CANCELED;
        }
    	return null;
    }

	@Override
	public String getModel() {
		return "dirac";
	}
}
