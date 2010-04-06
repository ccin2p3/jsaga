package fr.in2p3.jsaga.adaptor.job.monitor;

import fr.in2p3.jsaga.adaptor.job.SubState;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.SagaException;
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
    protected SagaException m_nativeCause;

    public JobStatus(String jobId, Object stateCode, String stateString, int returnCode) {
        this(jobId, stateCode, stateString);
        if (returnCode != 0) {
            m_nativeCause = new JobWrapperException(jobId, returnCode);
        }
    }
    public JobStatus(String jobId, Object stateCode, String stateString, String cause) {
        this(jobId, stateCode, stateString);
        if (cause != null) {
            m_nativeCause = new NoSuccessException(cause);
        }
    }
    public JobStatus(String jobId, Object stateCode, String stateString) {
        m_nativeJobId = jobId;
        m_nativeStateCode = stateCode;
        m_nativeStateString = stateString;
        m_nativeCause = null;
    }

    /**
     * @return the identifier of the job in the grid
     */
    public String getNativeJobId() {
        return m_nativeJobId;
    }

    /**
     * @return the saga state of the job
     */
    public State getSagaState() {
        return this.getSubState().toSagaState();
    }

    /**
     * @return the backend state of the job
     */
    public String getStateDetail() {
        return this.getModel()+":"+m_nativeStateString;
    }

    /**
     * @return the cause of failure
     */
    public SagaException getCause() {
        return m_nativeCause;
    }

    /**
     * @return the backend name
     */
    public abstract String getModel();

    /**
     * Get the implementation-specific but middleware-independant state of the job.
     * In addition to SAGA states, this methods may return states such as PRE_STAGING, POST_STAGING,
     * QUEUED, FAILED_ERROR and FAILED_ABORTED.
     * @return the JSAGA state of the job
     */
    public abstract SubState getSubState();
}
