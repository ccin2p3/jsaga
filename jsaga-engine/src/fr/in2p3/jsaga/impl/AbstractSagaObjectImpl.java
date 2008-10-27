package fr.in2p3.jsaga.impl;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.session.Session;

import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractSagaObjectImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractSagaObjectImpl implements SagaObject {
    protected Session m_session;
    private UUID m_uuid;

    /** constructor */
    public AbstractSagaObjectImpl(Session session) {
        m_session = session;
        m_uuid = UUID.randomUUID();
    }

    /** constructor */
    public AbstractSagaObjectImpl() {
        m_session = null;
        m_uuid = UUID.randomUUID();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSagaObjectImpl clone = (AbstractSagaObjectImpl) super.clone();
        clone.m_session = m_session;
        clone.m_uuid = UUID.randomUUID();
        return clone;
    }

    /////////////////////////////////////////// implementation ///////////////////////////////////////////

    public Session getSession() throws DoesNotExistException {
        if (m_session != null) {
            return m_session;
        } else {
            throw new DoesNotExistException("This object does not have a session attached", this);
        }
    }

    public String getId() {
        return m_uuid.toString();
    }
}
