package fr.in2p3.jsaga.adaptor.bes.job;


import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public class BesJobStatus extends JobStatus {

	public BesJobStatus(String jobId, ActivityStatusType activityStatus) {
		super(jobId, activityStatus, activityStatus.getState().getValue());
	}

	public BesJobStatus(String jobId, ActivityStatusType activityStatus, int returnCode) {
		super(jobId, activityStatus, activityStatus.getState().getValue(), returnCode);
	}

	public String getModel() {
        return "BES";
    }

	/**
	 * Gets the BES state of the job as found in the GetActivityStatusesResponseType message
	 * 
	 * For example:
	 * <ns1:ActivityStatus state="Running" xsi:type="ns1:ActivityStatusType">
     * </ns1:ActivityStatus>
	 * 
	 * @return JobStatus the substate 
	 */
	public SubState getSubState() {
    	ActivityStateEnumeration state = ((ActivityStatusType) m_nativeStateCode).getState();
    		
        if (ActivityStateEnumeration.Pending.equals(state)) {
            return SubState.RUNNING_SUBMITTED;
        } else if (ActivityStateEnumeration.Running.equals(state)) {
       		return SubState.RUNNING_ACTIVE;
        } else if (ActivityStateEnumeration.Finished.equals(state)) {
            return SubState.DONE;
        } else if (ActivityStateEnumeration.Cancelled.equals(state)) {
            return SubState.CANCELED;
        } else if (ActivityStateEnumeration.Failed.equals(state)) {
            return SubState.FAILED_ERROR;
        } else {
            return null;
        }
    }
}