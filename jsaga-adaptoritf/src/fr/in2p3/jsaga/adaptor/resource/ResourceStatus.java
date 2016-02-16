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
 * A class that represents the status of a resource.
 */
public abstract class ResourceStatus {
    protected Object m_nativeStateCode;
    protected String m_nativeStateString;
    protected SagaException m_nativeCause;

    /**
     * Build a status composed of a native state code, a native state description and a error description
     * 
     * @param stateCode
     * @param stateString
     * @param cause
     */
    public ResourceStatus(Object stateCode, String stateString, String cause) {
        this(stateCode, stateString);
        if (cause != null) {
            m_nativeCause = new NoSuccessException(cause);
        }
    }
    
    /**
     * Build a status composed of a native state code, a native state description
     * 
     * @param stateCode
     * @param stateString
     */
    public ResourceStatus(Object stateCode, String stateString) {
        m_nativeStateCode = stateCode;
        m_nativeStateString = stateString;
        m_nativeCause = null;
    }

    /**
     * Translate the native state into a SAGA state
     * @return the SAGA {@link State}
     */
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
     * The backend model is a string that identifies the type of backend.
     * 
     * @return the backend name
     */
    protected abstract String getModel();

}
