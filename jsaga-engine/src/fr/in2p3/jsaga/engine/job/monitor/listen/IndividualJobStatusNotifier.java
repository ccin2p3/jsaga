package fr.in2p3.jsaga.engine.job.monitor.listen;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IndividualJobStatusNotifier
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IndividualJobStatusNotifier implements JobStatusNotifier {
    private JobMonitorCallback m_callback;

    public IndividualJobStatusNotifier(JobMonitorCallback callback) {
        m_callback = callback;
    }

    public void notifyChange(JobStatus status) {
        m_callback.setState(status.getSagaState(), status.getStateDetail(), status.getSubState(), status.getCause());
    }
}
