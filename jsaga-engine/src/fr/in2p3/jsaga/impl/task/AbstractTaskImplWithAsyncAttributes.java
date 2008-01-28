package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.attributes.AbstractAsyncAttributesImpl;
import fr.in2p3.jsaga.impl.attributes.AttributeImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractTaskImplWithAsyncAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractTaskImplWithAsyncAttributes<E> extends AbstractTaskImpl<E> implements AsyncAttributes {
    private AbstractAsyncAttributesImpl m_attributes;

    /** constructor */
    public AbstractTaskImplWithAsyncAttributes(Session session, SagaObject object, boolean create) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, object, create);
        m_attributes = new AbstractAsyncAttributesImpl(m_session){
            public ObjectType getType() {
                return ObjectType.UNKNOWN;
            }
        };
    }

    public void setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        m_attributes.setAttribute(key, value);
    }

    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.getAttribute(key);
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        m_attributes.setVectorAttribute(key, values);
    }

    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.getVectorAttribute(key);
    }

    public void removeAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        m_attributes.removeAttribute(key);
    }

    public String[] listAttributes() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return m_attributes.listAttributes();
    }

    public String[] findAttributes(String... patterns) throws NotImplemented, BadParameter, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return m_attributes.findAttributes(patterns);
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.isReadOnlyAttribute(key);
    }

    public boolean isWritableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.isWritableAttribute(key);
    }

    public boolean isRemovableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.isRemovableAttribute(key);
    }

    public boolean isVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_attributes.isVectorAttribute(key);
    }

    public Task setAttribute(TaskMode mode, String key, String value) throws NotImplemented {
        return m_attributes.setAttribute(mode, key, value);
    }

    public Task<String> getAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.getAttribute(mode, key);
    }

    public Task setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplemented {
        return m_attributes.setVectorAttribute(mode, key, values);
    }

    public Task<String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.getVectorAttribute(mode, key);
    }

    public Task removeAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.removeAttribute(mode, key);
    }

    public Task<String[]> listAttributes(TaskMode mode) throws NotImplemented {
        return m_attributes.listAttributes(mode);
    }

    public Task<String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplemented {
        return m_attributes.findAttributes(mode, patterns);
    }

    public Task<Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.isReadOnlyAttribute(mode, key);
    }

    public Task<Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.isWritableAttribute(mode, key);
    }

    public Task<Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.isRemovableAttribute(mode, key);
    }

    public Task<Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        return m_attributes.isVectorAttribute(mode, key);
    }

    //////////////////////////////////////////// internal methods ////////////////////////////////////////////

    public AttributeImpl _addAttribute(AttributeImpl attribute) {
        return m_attributes._addAttribute(attribute);
    }
}
