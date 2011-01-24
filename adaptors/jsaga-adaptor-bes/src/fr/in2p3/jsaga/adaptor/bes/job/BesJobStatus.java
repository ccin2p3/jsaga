package fr.in2p3.jsaga.adaptor.bes.job;


import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public abstract class BesJobStatus extends JobStatus {

	public BesJobStatus(String jobId, ActivityStatusType activityStatus) {
		super(jobId, activityStatus, activityStatus.getState().getValue());
	}

	public String getModel() {
        return "BES";
    }

}