package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.permissions.AbstractDataPermissionsImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncNSEntryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncNSEntryImpl extends AbstractDataPermissionsImpl implements NSEntry {
    /** constructor */
    public AbstractAsyncNSEntryImpl(Session session, URL url, DataAdaptor adaptor) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor);
    }

    public Task<URL> getURL(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("getURL", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<URL> getCWD(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("getCWD", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<URL> getName(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("getName", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isDir(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("isDir", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isEntry(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("isEntry", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isLink(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("isLink", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<URL> readLink(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("readLink", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task copy(TaskMode mode, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("copy", new Class[]{URL.class, int.class}),
                    new Object[]{target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task copy(TaskMode mode, URL target) throws NotImplemented {
        return this.copy(mode, target, Flags.NONE.getValue());
    }

    public Task link(TaskMode mode, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("link", new Class[]{URL.class, int.class}),
                    new Object[]{target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task link(TaskMode mode, URL target) throws NotImplemented {
        return this.link(mode, target, Flags.NONE.getValue());
    }

    public Task move(TaskMode mode, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("move", new Class[]{URL.class, int.class}),
                    new Object[]{target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task move(TaskMode mode, URL target) throws NotImplemented {
        return this.move(mode, target, Flags.NONE.getValue());
    }

    public Task remove(TaskMode mode, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("remove", new Class[]{int.class}),
                    new Object[]{flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task remove(TaskMode mode) throws NotImplemented {
        return this.remove(mode, Flags.NONE.getValue());
    }

    public Task close(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("close", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task close(TaskMode mode, float timeoutInSeconds) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("close", new Class[]{float.class}),
                    new Object[]{timeoutInSeconds}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task permissionsAllow(TaskMode mode, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("permissionsAllow", new Class[]{String.class, int.class, int.class}),
                    new Object[]{id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task permissionsDeny(TaskMode mode, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImpl.class.getMethod("permissionsDeny", new Class[]{String.class, int.class, int.class}),
                    new Object[]{id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
