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
    private long m_submitTime;

    public EmulatorJobStatus(String jobId, SubState state) {
        super(jobId, state, state.toString());
        m_submitTime = System.currentTimeMillis();
    }

    public EmulatorJobStatus(String jobId, SubState state, String cause) {
        super(jobId, state, state.toString(), cause);
        m_submitTime = System.currentTimeMillis();
    }

    public String getModel() {
        return "Emulator";
    }

    public SubState getSubState() {
        return (SubState) m_nativeStateCode;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - m_submitTime;
    }
}
