package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.NotImplemented;
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
public abstract class AbstractAsyncAttributesImpl extends AbstractAttributesImpl implements AsyncAttributes {
    public AbstractAsyncAttributesImpl(Session session) {
        super(session);
    }

    public Task setAttribute(TaskMode mode, String key, String value) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("setAttribute", new Class[]{String.class, String.class}),
                    new Object[]{key, value}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String> getAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("getAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("setVectorAttribute", new Class[]{String.class, String[].class}),
                    new Object[]{key, values}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("getVectorAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task removeAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("removeAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String[]> listAttributes(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("listAttributes", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("findAttributes", new Class[]{String[].class}),
                    new Object[]{patterns}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("isReadOnlyAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("isWritableAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("isRemovableAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractAttributesImpl.class.getMethod("isVectorAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
