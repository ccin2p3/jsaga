package fr.in2p3.jsaga.adaptor.resource;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.resource.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ResourceStatus
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   10 FEB 2016
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ResourceStatus {
//    private String m_nativeResourceId;
//    protected State m_sagaState;
    protected Object m_nativeStateCode;
    protected String m_nativeStateString;
    protected SagaException m_nativeCause;

    // TODO remove param state
    public ResourceStatus(State state, Object stateCode, String stateString, int returnCode) {
        this(state, stateCode, stateString);
        if (returnCode != 0) {
            m_nativeCause = new NoSuccessException(stateString);
        }
    }
    public ResourceStatus(State state, Object stateCode, String stateString, String cause) {
        this(state, stateCode, stateString);
        if (cause != null) {
            m_nativeCause = new NoSuccessException(cause);
        }
    }
    public ResourceStatus(State state, Object stateCode, String stateString) {
//        m_nativeResourceId = resourceId;
//        m_sagaState = state;
        m_nativeStateCode = stateCode;
        m_nativeStateString = stateString;
        m_nativeCause = null;
    }

    /**
     * @return the identifier of the resource in the cloud
     */
//    public String getNativeResourceId() {
//        return m_nativeResourceId;
//    }

    public abstract State getSagaState();
    /**
     * @return the backend state of the resource
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
    protected abstract String getModel();

}
