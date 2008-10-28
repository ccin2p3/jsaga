package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorJobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorJobStatus extends JobStatus {
    public EmulatorJobStatus(String jobId, SubState stateCode) {
        super(jobId, stateCode, stateCode.toString());
    }

    public EmulatorJobStatus(String jobId, SubState stateCode, Exception exception) {
        super(jobId, stateCode, stateCode.toString(), exception.getMessage());
    }

    public String getModel() {
        return "Emulator";
    }

    public SubState getSubState() {
        return (SubState) m_nativeStateCode;
    }
}
