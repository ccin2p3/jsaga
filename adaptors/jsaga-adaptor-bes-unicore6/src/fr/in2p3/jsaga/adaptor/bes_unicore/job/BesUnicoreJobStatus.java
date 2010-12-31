package fr.in2p3.jsaga.adaptor.bes_unicore.job;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 déc 2010
* ***************************************************/

public class BesUnicoreJobStatus extends BesJobStatus {

	public BesUnicoreJobStatus(String jobId, ActivityStatusType activityStatus) {
		super(jobId, activityStatus);
	}

	/*public String getModel() {
        return "TODO";
    }*/

    public SubState getSubState() {
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
    }
}
