package fr.in2p3.jsaga.impl;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.lang.Exception;
import java.util.*;

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

    public Session getSession() throws DoesNotExist {
        if (m_session != null) {
            return m_session;
        } else {
            throw new DoesNotExist("This object does not have a session attached", this);
        }
    }

    public String getId() {
        return m_uuid.toString();
    }

    public abstract ObjectType getType();

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public static Task prepareTask(TaskMode mode, Task task) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        switch(mode) {
            case TASK:
                return task;
            case ASYNC:
                task.run();
                return task;
            case SYNC:
                task.run();
                task.waitFor();
                return task;
            default:
                throw new NotImplemented("INTERNAL ERROR: unexpected exception");
        }        
    }

    // helpers
    protected static Map clone(Map source) throws CloneNotSupportedException {
        Map clone = new HashMap();
        for (Iterator it=source.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            SagaObject value = (SagaObject) entry.getValue();
            SagaObject valueClone = (SagaObject) value.clone();
            clone.put(key, valueClone);
        }
        return clone;
    }
    protected static List clone(List source) throws CloneNotSupportedException {
        List clone = new ArrayList();
        for (Iterator it=source.iterator(); it.hasNext(); ) {
            SagaObject value = (SagaObject) it.next();
            SagaObject valueClone = (SagaObject) value.clone();
            clone.add(valueClone);
        }
        return clone;
    }

    /**
     * deeply copy a castor bean
     * @param source the source castor bean
     * @return the target castor bean
     */
    protected static Serializable clone(Serializable source) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(source, doc);
            return (Serializable) Unmarshaller.unmarshal(source.getClass(), doc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
