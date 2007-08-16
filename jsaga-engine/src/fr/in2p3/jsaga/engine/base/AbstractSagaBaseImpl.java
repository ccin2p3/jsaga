package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.SagaBase;
import org.ogf.saga.session.Session;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractSagaBaseImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractSagaBaseImpl implements SagaBase {
    protected Session m_session;

    /** constructor */
    public AbstractSagaBaseImpl(Session session) {
        m_session = session;
    }
    /** constructor */
    public AbstractSagaBaseImpl() {
        m_session = null;
    }

    /** constructor for deepCopy */
    protected AbstractSagaBaseImpl(AbstractSagaBaseImpl source) {
        m_session = source.m_session;
    }

    public Session getSession() {
        return m_session;
    }

    public abstract SagaBase deepCopy();

    public int getId() {
        return hashCode();
    }

    // helpers
    protected static Map deepCopy(Map source) {
        Map clone = new HashMap();
        for (Iterator it=source.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String keyClone = (String) entry.getKey();
            SagaBase valueClone = ((SagaBase) entry.getValue()).deepCopy();
            clone.put(keyClone, valueClone);
        }
        return clone;
    }
    protected static List deepCopy(List source) {
        List clone = new ArrayList();
        for (Iterator it=source.iterator(); it.hasNext(); ) {
            SagaBase valueClone = ((SagaBase) it.next()).deepCopy();
            clone.add(valueClone);
        }
        return clone;
    }
}
