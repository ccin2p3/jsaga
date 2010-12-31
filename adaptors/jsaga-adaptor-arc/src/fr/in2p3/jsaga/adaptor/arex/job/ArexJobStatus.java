package fr.in2p3.jsaga.adaptor.arex.job;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.nordugrid.schemas.arex.ActivitySubStateType;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesArexJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 déc 2010
* ***************************************************/

public class ArexJobStatus extends BesJobStatus {

	public ArexJobStatus(String jobId, ActivityStatusType activityStatus) {
		super(jobId, activityStatus);
	}

	/*public String getModel() {
        return "TODO";
    }*/

    public SubState getSubState() {
    	ActivityStateEnumeration state = ((ActivityStatusType) m_nativeStateCode).getState();
    	String substate = null;
		for (MessageElement me: ((ActivityStatusType) m_nativeStateCode).get_any()) {
			if ("State".equals(me.getName()) && ArexJobControlAdaptor.AREX_NAMESPACE_URI.equals(me.getNamespaceURI())) {
				substate = me.getFirstChild().getNodeValue();
			}
		}
        if (ActivityStateEnumeration.Pending.equals(state)) {
       		return SubState.NEW_CREATED;
        } else if (ActivityStateEnumeration.Running.equals(state)) {
        	if (ActivitySubStateType.Submitting.equals(substate) ||
        			ActivitySubStateType.Preparing.equals(substate) ||
        			ActivitySubStateType.Prepared.equals(substate)) {
        		return SubState.RUNNING_SUBMITTED;
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
