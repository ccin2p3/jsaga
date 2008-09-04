package fr.in2p3.jsaga.adaptor.naregi.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SuperSchedulerJobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SuperSchedulerJobStatus extends JobStatus {
    private static final String RUNNING = "Running";
    private static final String DONE = "Done";

    public SuperSchedulerJobStatus(String jobId, String stateString) {
        super(jobId, stateString, stateString);
    }

    public String getModel() {
        return "NAREGI";
    }

    public SubState getSubState() {
        if (RUNNING.equals(m_nativeStateString)) {
            return SubState.RUNNING_ACTIVE;
        } else if (DONE.equals(m_nativeStateString)) {
            return SubState.DONE;
        } else {
System.err.println("Unknown status: "+m_nativeStateString); //todo: remove this line
            return SubState.FAILED_ERROR;
        }
    }
}