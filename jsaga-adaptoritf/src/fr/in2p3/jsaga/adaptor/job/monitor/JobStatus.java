package fr.in2p3.jsaga.adaptor.job.monitor;

import fr.in2p3.jsaga.adaptor.job.SubState;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class JobStatus {
    private String m_nativeJobId;
    protected Object m_nativeStateCode;
    protected String m_nativeStateString;
    protected String m_nativeCause;

    public JobStatus(String jobId, Object stateCode, String stateString) {
        this(jobId, stateCode, stateString, null);
    }
    public JobStatus(String jobId, Object stateCode, String stateString, String cause) {
        m_nativeJobId = jobId;
        m_nativeStateCode = stateCode;
        m_nativeStateString = stateString;
        m_nativeCause = cause;
    }

    public String getNativeJobId() {
        return m_nativeJobId;
    }

    public State getSagaState() {
        return this.getSubState().toSagaState();
    }

    public String getStateDetail() {
        return this.getModel()+":"+m_nativeStateString;
    }

    public abstract String getModel();
    public abstract SubState getSubState();
}
