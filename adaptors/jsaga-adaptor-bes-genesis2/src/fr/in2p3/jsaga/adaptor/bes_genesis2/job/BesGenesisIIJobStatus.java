package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;
import fr.in2p3.jsaga.adaptor.job.SubState;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
* ***************************************************/

public class BesGenesisIIJobStatus extends BesJobStatus {

	public BesGenesisIIJobStatus(String jobId, ActivityStatusType activityStatus) {
		super(jobId, activityStatus);
	}

	/**
	 * Gets the GenesisII substate of the job as found in the GetActivityStatusesResponseType message
	 * 
	 * TODO: modify DOC
	 * For example:
	 * <ns1:ActivityStatus state="Running" xsi:type="ns1:ActivityStatusType">
	 *   <ns7:Exiting xmlns:ns7="http://vcgr.cs.virginia.edu/genesisII/bes/activity-states"/>
     * </ns1:ActivityStatus>
	 * 
	 * @return JobStatus the substate 
	 */
	public SubState getSubState() {
    	ActivityStateEnumeration state = ((ActivityStatusType) m_nativeStateCode).getState();
    	MessageElement any[] = ((ActivityStatusType) m_nativeStateCode).get_any();
    	String substate = (any == null)?null:any[0].getName();
    		
        if (ActivityStateEnumeration.Pending.equals(state)) {
            return SubState.RUNNING_SUBMITTED;
        } else if (ActivityStateEnumeration.Running.equals(state)) {
        	if ("Queued".equals(substate) || "Enqueing".equals(substate)) {
        		return SubState.RUNNING_SUBMITTED;
        	} else if ("Exiting".equals(substate)) {
        		return SubState.RUNNING_ACTIVE;
        	} else {
        		return SubState.RUNNING_ACTIVE;
        	}
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
