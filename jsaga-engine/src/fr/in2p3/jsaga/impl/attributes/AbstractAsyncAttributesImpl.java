package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncAttributesImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncAttributesImpl<T> extends AbstractAttributesImpl implements AsyncAttributes<T> {
    private T m_object;

    public AbstractAsyncAttributesImpl(Session session, T object) {
        super(session);
        m_object = object;
    }

    public Task<T, Void> setAttribute(TaskMode mode, String key, String value) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Void>().create(
                mode, m_session, m_object,
                "setAttribute",
                new Class[]{String.class, String.class},
                new Object[]{key, value});
    }

    public Task<T, String> getAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,String>().create(
                mode, m_session, m_object,
                "getAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Void>().create(
                mode, m_session, m_object,
                "setVectorAttribute",
                new Class[]{String.class, String[].class},
                new Object[]{key, values});
    }

    public Task<T, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,String[]>().create(
                mode, m_session, m_object,
                "getVectorAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, Void> removeAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Void>().create(
                mode, m_session, m_object,
                "removeAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,String[]>().create(
                mode, m_session, m_object,
                "listAttributes",
                new Class[]{},
                new Object[]{});
    }

    public Task<T, String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,String[]>().create(
                mode, m_session, m_object,
                "findAttributes",
                new Class[]{String[].class},
                new Object[]{patterns});
    }

    public Task<T, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Boolean>().create(
                mode, m_session, m_object,
                "isReadOnlyAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Boolean>().create(
                mode, m_session, m_object,
                "isWritableAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Boolean>().create(
                mode, m_session, m_object,
                "isRemovableAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<T, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<T,Boolean>().create(
                mode, m_session, m_object,
                "isVectorAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }
}
