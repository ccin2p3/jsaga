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
    protected Object m_nativeStateCode;
    protected String m_nativeStateString;
    protected SagaException m_nativeCause;

    public ResourceStatus(Object stateCode, String stateString, int returnCode) {
        this(stateCode, stateString);
        if (returnCode != 0) {
            m_nativeCause = new NoSuccessException(stateString);
        }
    }
    public ResourceStatus(Object stateCode, String stateString, String cause) {
        this(stateCode, stateString);
        if (cause != null) {
            m_nativeCause = new NoSuccessException(cause);
        }
    }
    public ResourceStatus(Object stateCode, String stateString) {
        m_nativeStateCode = stateCode;
        m_nativeStateString = stateString;
        m_nativeCause = null;
    }

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
