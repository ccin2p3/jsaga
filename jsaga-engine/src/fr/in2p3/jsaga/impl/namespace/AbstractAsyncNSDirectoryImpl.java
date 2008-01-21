package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncNSDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncNSDirectoryImpl extends AbstractNSEntryDirImpl implements NSDirectory {
    /** constructor for factory */
    public AbstractAsyncNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for open() */
    public AbstractAsyncNSDirectoryImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry, url, flags);
    }

    public Task changeDir(TaskMode mode, URL dir) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("changeDir", new Class[]{URL.class}),
                    new Object[]{dir}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<List<URL>> list(TaskMode mode, String pattern, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("list", new Class[]{String.class, int.class}),
                    new Object[]{pattern, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<List<URL>> list(TaskMode mode, String pattern) throws NotImplemented {
        return this.list(mode, pattern, Flags.NONE.getValue());
    }

    public Task<List<URL>> list(TaskMode mode, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("list", new Class[]{int.class}),
                    new Object[]{flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<List<URL>> list(TaskMode mode) throws NotImplemented {
        return this.list(mode, Flags.NONE.getValue());
    }

    public Task<List<URL>> find(TaskMode mode, String pattern, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("find", new Class[]{String.class, int.class}),
                    new Object[]{pattern, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<List<URL>> find(TaskMode mode, String pattern) throws NotImplemented {
        return this.find(mode, pattern, Flags.RECURSIVE.getValue());
    }

    public Task<Boolean> exists(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("exists", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isDir(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("isDir", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isEntry(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("isEntry", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isLink(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("isLink", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<URL> readLink(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("readLink", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> getNumEntries(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("getNumEntries", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<URL> getEntry(TaskMode mode, int entry) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("getEntry", new Class[]{int.class}),
                    new Object[]{entry}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task copy(TaskMode mode, URL source, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("copy", new Class[]{URL.class, URL.class, int.class}),
                    new Object[]{source, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task copy(TaskMode mode, URL source, URL target) throws NotImplemented {
        return this.copy(mode, source, target, Flags.NONE.getValue());
    }

    public Task copy(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("copy", new Class[]{String.class, URL.class, int.class}),
                    new Object[]{sourcePattern, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task copy(TaskMode mode, String sourcePattern, URL target) throws NotImplemented {
        return this.copy(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task link(TaskMode mode, URL source, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("link", new Class[]{URL.class, URL.class, int.class}),
                    new Object[]{source, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task link(TaskMode mode, URL source, URL target) throws NotImplemented {
        return this.link(mode, source, target, Flags.NONE.getValue());
    }

    public Task link(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("link", new Class[]{String.class, URL.class, int.class}),
                    new Object[]{sourcePattern, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task link(TaskMode mode, String sourcePattern, URL target) throws NotImplemented {
        return this.link(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task move(TaskMode mode, URL source, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("move", new Class[]{URL.class, URL.class, int.class}),
                    new Object[]{source, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task move(TaskMode mode, URL source, URL target) throws NotImplemented {
        return this.move(mode, source, target, Flags.NONE.getValue());
    }

    public Task move(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("move", new Class[]{String.class, URL.class, int.class}),
                    new Object[]{sourcePattern, target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task move(TaskMode mode, String sourcePattern, URL target) throws NotImplemented {
        return this.move(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task remove(TaskMode mode, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("remove", new Class[]{URL.class, int.class}),
                    new Object[]{target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task remove(TaskMode mode, URL target) throws NotImplemented {
        return this.remove(mode, target, Flags.NONE.getValue());
    }

    public Task remove(TaskMode mode, String targetPattern, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("remove", new Class[]{String.class, int.class}),
                    new Object[]{targetPattern, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task remove(TaskMode mode, String targetPattern) throws NotImplemented {
        return this.remove(mode, targetPattern, Flags.NONE.getValue());
    }

    public Task makeDir(TaskMode mode, URL target, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("makeDir", new Class[]{URL.class, int.class}),
                    new Object[]{target, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task makeDir(TaskMode mode, URL target) throws NotImplemented {
        return this.makeDir(mode, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory> openDir(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("openDir", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<NSDirectory> openDir(TaskMode mode, URL name) throws NotImplemented {
        return this.openDir(mode, name, Flags.NONE.getValue());
    }

    public Task<NSEntry> open(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("open", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<NSEntry> open(TaskMode mode, URL name) throws NotImplemented {
        return this.open(mode, name, Flags.NONE.getValue());
    }

    public Task permissionsAllow(TaskMode mode, URL target, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("permissionsAllow", new Class[]{URL.class, String.class, int.class, int.class}),
                    new Object[]{target, id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task permissionsAllow(TaskMode mode, URL target, String id, int permissions) throws NotImplemented {
        return this.permissionsAllow(mode, target, id, permissions, Flags.NONE.getValue());
    }

    public Task permissionsAllow(TaskMode mode, String targetPattern, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("permissionsAllow", new Class[]{String.class, String.class, int.class, int.class}),
                    new Object[]{targetPattern, id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task permissionsAllow(TaskMode mode, String targetPattern, String id, int permissions) throws NotImplemented {
        return this.permissionsAllow(mode, targetPattern, id, permissions, Flags.NONE.getValue());
    }

    public Task permissionsDeny(TaskMode mode, URL target, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("permissionsDeny", new Class[]{URL.class, String.class, int.class, int.class}),
                    new Object[]{target, id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task permissionsDeny(TaskMode mode, URL target, String id, int permissions) throws NotImplemented {
        return this.permissionsDeny(mode, target, id, permissions, Flags.NONE.getValue());
    }

    public Task permissionsDeny(TaskMode mode, String targetPattern, String id, int permissions, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSDirectoryImpl.class.getMethod("permissionsDeny", new Class[]{String.class, String.class, int.class, int.class}),
                    new Object[]{targetPattern, id, permissions, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task permissionsDeny(TaskMode mode, String targetPattern, String id, int permissions) throws NotImplemented {
        return this.permissionsDeny(mode, targetPattern, id, permissions, Flags.NONE.getValue());
    }
}
