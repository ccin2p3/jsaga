package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;
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
public abstract class AbstractAsyncAttributesImpl<T extends Attributes> extends AbstractAttributesImpl implements AsyncAttributes<T> {
    private T m_object;

    public AbstractAsyncAttributesImpl(Session session, T object) {
        super(session);
        m_object = object;
    }

    public Task<T, Void> setAttribute(TaskMode mode, final String key, final String value) throws NotImplementedException {
        return new AbstractThreadedTask<T,Void>(mode){
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                m_object.setAttribute(key, value);
                return null;
            }
        };
    }

    public Task<T, String> getAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,String>(mode) {
            public String invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.getAttribute(key);
            }
        };
    }

    public Task<T, Void> setVectorAttribute(TaskMode mode, final String key, final String[] values) throws NotImplementedException {
        return new AbstractThreadedTask<T,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                m_object.setVectorAttribute(key, values);
                return null;
            }
        };
    }

    public Task<T, String[]> getVectorAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,String[]>(mode) {
            public String[] invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.getVectorAttribute(key);
            }
        };
    }

    public Task<T, Void> removeAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                m_object.removeAttribute(key);
                return null;
            }
        };
    }

    public Task<T, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<T,String[]>(mode) {
            public String[] invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.listAttributes();
            }
        };
    }

    public Task<T, String[]> findAttributes(TaskMode mode, final String... patterns) throws NotImplementedException {
        return new AbstractThreadedTask<T,String[]>(mode) {
            public String[] invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.findAttributes(patterns);
            }
        };
    }

    public Task<T, Boolean> existsAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.existsAttribute(key);
            }
        };
    }

    public Task<T, Boolean> isReadOnlyAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.isReadOnlyAttribute(key);
            }
        };
    }

    public Task<T, Boolean> isWritableAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.isWritableAttribute(key);
            }
        };
    }

    public Task<T, Boolean> isRemovableAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.isRemovableAttribute(key);
            }
        };
    }

    public Task<T, Boolean> isVectorAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return m_object.isVectorAttribute(key);
            }
        };
    }
}
