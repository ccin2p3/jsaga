package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
public abstract class AbstractAsyncNSDirectoryImpl extends AbstractSyncNSDirectoryImpl implements NSDirectory {
    /** constructor for factory */
    protected AbstractAsyncNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncNSDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncNSDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////////// interface NSDirectory //////////////////////////////////////////

    public Task<NSDirectory, Void> changeDir(TaskMode mode, URL dir) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "changeDirSync",
                new Class[]{URL.class},
                new Object[]{dir});
    }

    public Task<NSDirectory, List<URL>> list(TaskMode mode, String pattern, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,List<URL>>().create(
                mode, m_session, this,
                "listSync",
                new Class[]{String.class, int.class},
                new Object[]{pattern, flags});
    }
    public Task<NSDirectory, List<URL>> list(TaskMode mode, String pattern) throws NotImplementedException {
        return this.list(mode, pattern, Flags.NONE.getValue());
    }

    public Task<NSDirectory, List<URL>> list(TaskMode mode, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,List<URL>>().create(
                mode, m_session, this,
                "listSync",
                new Class[]{int.class},
                new Object[]{flags});
    }
    public Task<NSDirectory, List<URL>> list(TaskMode mode) throws NotImplementedException {
        return this.list(mode, Flags.NONE.getValue());
    }

    public Task<NSDirectory, List<URL>> find(TaskMode mode, String pattern, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,List<URL>>().create(
                mode, m_session, this,
                "findSync",
                new Class[]{String.class, int.class},
                new Object[]{pattern, flags});
    }
    public Task<NSDirectory, List<URL>> find(TaskMode mode, String pattern) throws NotImplementedException {
        return this.find(mode, pattern, Flags.RECURSIVE.getValue());
    }

    public Task<NSDirectory, Boolean> exists(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Boolean>().create(
                mode, m_session, this,
                "existsSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<NSDirectory, Boolean> isDir(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Boolean>().create(
                mode, m_session, this,
                "isDirSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<NSDirectory, Boolean> isEntry(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Boolean>().create(
                mode, m_session, this,
                "isEntrySync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<NSDirectory, Boolean> isLink(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Boolean>().create(
                mode, m_session, this,
                "isLinkSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<NSDirectory, URL> readLink(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,URL>().create(
                mode, m_session, this,
                "readLinkSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<NSDirectory, Integer> getNumEntries(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Integer>().create(
                mode, m_session, this,
                "getNumEntriesSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSDirectory, URL> getEntry(TaskMode mode, int entry) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,URL>().create(
                mode, m_session, this,
                "getEntrySync",
                new Class[]{int.class},
                new Object[]{entry});
    }

    public Task<NSDirectory, Void> copy(TaskMode mode, URL source, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "copySync",
                new Class[]{URL.class, URL.class, int.class},
                new Object[]{source, target, flags});
    }
    public Task<NSDirectory, Void> copy(TaskMode mode, URL source, URL target) throws NotImplementedException {
        return this.copy(mode, source, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> copy(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "copySync",
                new Class[]{String.class, URL.class, int.class},
                new Object[]{sourcePattern, target, flags});
    }
    public Task<NSDirectory, Void> copy(TaskMode mode, String sourcePattern, URL target) throws NotImplementedException {
        return this.copy(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> link(TaskMode mode, URL source, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "linkSync",
                new Class[]{URL.class, URL.class, int.class},
                new Object[]{source, target, flags});
    }
    public Task<NSDirectory, Void> link(TaskMode mode, URL source, URL target) throws NotImplementedException {
        return this.link(mode, source, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> link(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "linkSync",
                new Class[]{String.class, URL.class, int.class},
                new Object[]{sourcePattern, target, flags});
    }
    public Task<NSDirectory, Void> link(TaskMode mode, String sourcePattern, URL target) throws NotImplementedException {
        return this.link(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> move(TaskMode mode, URL source, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "moveSync",
                new Class[]{URL.class, URL.class, int.class},
                new Object[]{source, target, flags});
    }
    public Task<NSDirectory, Void> move(TaskMode mode, URL source, URL target) throws NotImplementedException {
        return this.move(mode, source, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> move(TaskMode mode, String sourcePattern, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "moveSync",
                new Class[]{String.class, URL.class, int.class},
                new Object[]{sourcePattern, target, flags});
    }
    public Task<NSDirectory, Void> move(TaskMode mode, String sourcePattern, URL target) throws NotImplementedException {
        return this.move(mode, sourcePattern, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> remove(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "removeSync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }
    public Task<NSDirectory, Void> remove(TaskMode mode, URL target) throws NotImplementedException {
        return this.remove(mode, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> remove(TaskMode mode, String targetPattern, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "removeSync",
                new Class[]{String.class, int.class},
                new Object[]{targetPattern, flags});
    }
    public Task<NSDirectory, Void> remove(TaskMode mode, String targetPattern) throws NotImplementedException {
        return this.remove(mode, targetPattern, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> makeDir(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "makeDirSync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }
    public Task<NSDirectory, Void> makeDir(TaskMode mode, URL target) throws NotImplementedException {
        return this.makeDir(mode, target, Flags.NONE.getValue());
    }

    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,NSDirectory>().create(
                mode, m_session, this,
                "openDir",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, URL name) throws NotImplementedException {
        return this.openDir(mode, name, Flags.NONE.getValue());
    }

    public Task<NSDirectory, NSEntry> open(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,NSEntry>().create(
                mode, m_session, this,
                "open",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<NSDirectory, NSEntry> open(TaskMode mode, URL name) throws NotImplementedException {
        return this.open(mode, name, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, URL target, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "permissionsAllowSync",
                new Class[]{URL.class, String.class, int.class, int.class},
                new Object[]{target, id, permissions, flags});
    }
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, URL target, String id, int permissions) throws NotImplementedException {
        return this.permissionsAllow(mode, target, id, permissions, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, String targetPattern, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "permissionsAllowSync",
                new Class[]{String.class, String.class, int.class, int.class},
                new Object[]{targetPattern, id, permissions, flags});
    }
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, String targetPattern, String id, int permissions) throws NotImplementedException {
        return this.permissionsAllow(mode, targetPattern, id, permissions, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, URL target, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "permissionsDenySync",
                new Class[]{URL.class, String.class, int.class, int.class},
                new Object[]{target, id, permissions, flags});
    }
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, URL target, String id, int permissions) throws NotImplementedException {
        return this.permissionsDeny(mode, target, id, permissions, Flags.NONE.getValue());
    }

    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, String targetPattern, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Void>().create(
                mode, m_session, this,
                "permissionsDenySync",
                new Class[]{String.class, String.class, int.class, int.class},
                new Object[]{targetPattern, id, permissions, flags});
    }
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, String targetPattern, String id, int permissions) throws NotImplementedException {
        return this.permissionsDeny(mode, targetPattern, id, permissions, Flags.NONE.getValue());
    }

    /////////////////////////////////////// override some methods of NSEntry ///////////////////////////////////////

    /** override super.getCWD() */
    public Task<NSEntry, URL> getCWD(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,URL>().create(
                mode, m_session, this,
                "getCWDSync",
                new Class[]{},
                new Object[]{});
    }

    /** override super.copy() */
    public Task<NSEntry, Void> copy(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "copySync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }

    /** override super.move() */
    public Task<NSEntry, Void> move(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "moveSync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }

    /** override super.remove() */
    public Task<NSEntry, Void> remove(TaskMode mode, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "removeSync",
                new Class[]{int.class},
                new Object[]{flags});
    }
}
