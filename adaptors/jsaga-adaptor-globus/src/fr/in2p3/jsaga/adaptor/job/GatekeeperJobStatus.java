package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import org.globus.gram.internal.GRAMConstants;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GatekeeperJobStatus extends JobStatus {
    public GatekeeperJobStatus(String jobId, int stateCode, String stateString) {
        super(jobId, stateCode, stateString);
    }

    public GatekeeperJobStatus(String jobId, int stateCode, String stateString, String cause) {
        super(jobId, stateCode, stateString, cause);
    }

    public String getModel() {
        return "GLOBUS";
    }

    public SubState getSubState() {
        switch(m_nativeStateCode) {
            case GRAMConstants.STATUS_UNSUBMITTED:
                return SubState.SUBMITTED;

            case GRAMConstants.STATUS_PENDING:
            case GRAMConstants.STATUS_STAGE_IN:
                return SubState.RUNNING_QUEUED;

            case GRAMConstants.STATUS_ACTIVE:
            case GRAMConstants.STATUS_STAGE_OUT:
                return SubState.RUNNING_ACTIVE;

            case GRAMConstants.STATUS_SUSPENDED:
                return SubState.SUSPENDED_QUEUED;

            case GRAMConstants.STATUS_DONE:
                return SubState.DONE;

            case GRAMConstants.STATUS_FAILED:
                return SubState.FAILED_ERROR;

            default:
                return null;
        }
    }
}
