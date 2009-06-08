package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.attributes.AbstractAsyncAttributesImpl;
import fr.in2p3.jsaga.impl.attributes.AttributeImpl;
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
public abstract class AbstractTaskImplWithAsyncAttributes<T,E,A extends SagaObject> extends AbstractTaskImpl<T,E> implements AsyncAttributes<A> {
    private AbstractAsyncAttributesImpl<A> m_attributes;

    /** constructor */
    public AbstractTaskImplWithAsyncAttributes(Session session, boolean create) throws NotImplementedException {
        super(session, null, create);
        m_attributes = new AbstractAsyncAttributesImpl<A>(m_session, (A) this){};
    }

    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_attributes.setAttribute(key, value);
    }

    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.getAttribute(key);
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_attributes.setVectorAttribute(key, values);
    }

    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.getVectorAttribute(key);
    }

    public void removeAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_attributes.removeAttribute(key);
    }

    public String[] listAttributes() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_attributes.listAttributes();
    }

    public String[] findAttributes(String... patterns) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_attributes.findAttributes(patterns);
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.isReadOnlyAttribute(key);
    }

    public boolean isWritableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.isWritableAttribute(key);
    }

    public boolean isRemovableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.isRemovableAttribute(key);
    }

    public boolean isVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_attributes.isVectorAttribute(key);
    }

    public Task<A, Void> setAttribute(TaskMode mode, String key, String value) throws NotImplementedException {
        return m_attributes.setAttribute(mode, key, value);
    }

    public Task<A, String> getAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.getAttribute(mode, key);
    }

    public Task<A, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        return m_attributes.setVectorAttribute(mode, key, values);
    }

    public Task<A, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.getVectorAttribute(mode, key);
    }

    public Task<A, Void> removeAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.removeAttribute(mode, key);
    }

    public Task<A, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return m_attributes.listAttributes(mode);
    }

    public Task<A, String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplementedException {
        return m_attributes.findAttributes(mode, patterns);
    }

    public Task<A, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.isReadOnlyAttribute(mode, key);
    }

    public Task<A, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.isWritableAttribute(mode, key);
    }

    public Task<A, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.isRemovableAttribute(mode, key);
    }

    public Task<A, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_attributes.isVectorAttribute(mode, key);
    }

    //////////////////////////////////////////// internal methods ////////////////////////////////////////////

    public AttributeImpl _addAttribute(AttributeImpl attribute) {
        return m_attributes._addAttribute(attribute);
    }
}
