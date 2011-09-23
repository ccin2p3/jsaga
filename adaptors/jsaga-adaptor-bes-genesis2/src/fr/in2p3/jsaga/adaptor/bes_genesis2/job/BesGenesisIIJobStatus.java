package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;

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
	 *   <fs:Staging-In xmlns:fs="http://schemas.ogf.org/hpcp/2007/01/fs"/>
     * </ns1:ActivityStatus>
	 * 
	 * @return JobStatus the substate 
	 */
	/*public SubState getSubState() {
		// TODO: modify this
    	ActivityStateEnumeration state = ((ActivityStatusType) m_nativeStateCode).getState();
    	MessageElement any[] = ((ActivityStatusType) m_nativeStateCode).get_any();
    	String substate = (any == null)?null:any[0].getName();
    		
        if (ActivityStateEnumeration.Pending.equals(state)) {
            return SubState.RUNNING_SUBMITTED;
        } else if (ActivityStateEnumeration.Running.equals(state)) {
        	if ("Staging-In".equals(substate)) {
        		return SubState.RUNNING_PRE_STAGING;
        	} else if ("Staging-Out".equals(substate)) {
        		return SubState.RUNNING_POST_STAGING;
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
    }*/
}
