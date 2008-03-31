package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.*;

import org.apache.log4j.Logger;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.internal.GRAMConstants;
import org.ietf.jgss.GSSException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobStatusListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GatekeeperJobStatusListener extends JobStatusListener implements GramJobListener {
	private Logger logger = Logger.getLogger(GatekeeperJobStatusListener.class.getName());
	
	public GatekeeperJobStatusListener(JobStatusNotifier notifier) {
        super(notifier);
    }

    public void statusChanged(GramJob job) {
    	// TODO : move to cleanup    	
    	if(GatekeeperJobAdaptorAbstract.twoPhaseUsed && 
    			(job.getStatus() == GRAMConstants.STATUS_DONE ||
    			job.getStatus() == GRAMConstants.STATUS_FAILED )) {
    		try {
    			// Send signal to clean jobmanager
    			job.signal(GRAMConstants.SIGNAL_COMMIT_END);
			} catch (GramException e) {
				logger.warn("Unable to send commit end signal", e);
			} catch (GSSException e) {
				logger.warn("Unable to send commit end signal", e);
			}
    	}
        JobStatus status = new GatekeeperJobStatus(job.getIDAsString(), new Integer(job.getStatus()), job.getStatusAsString());
        m_notifier.notifyChange(status);
    }
}
