package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.file.DirectoryImpl;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.file.copy.*;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
public abstract class AbstractAsyncNSEntryImpl extends AbstractSyncNSEntryImpl implements NSEntry {
    /** constructor for factory */
    protected AbstractAsyncNSEntryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncNSEntryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncNSEntryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    //////////////////////////////////////////// interface NSEntry ////////////////////////////////////////////

    public Task<NSEntry, URL> getURL(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,URL>().create(
                mode, m_session, this,
                "getURLSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, URL> getCWD(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,URL>().create(
                mode, m_session, this,
                "getCWDSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, URL> getName(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,URL>().create(
                mode, m_session, this,
                "getNameSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, Boolean> isDir(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Boolean>().create(
                mode, m_session, this,
                "isDirSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, Boolean> isEntry(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Boolean>().create(
                mode, m_session, this,
                "isEntrySync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, Boolean> isLink(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Boolean>().create(
                mode, m_session, this,
                "isLinkSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, URL> readLink(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,URL>().create(
                mode, m_session, this,
                "readLinkSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, Void> copy(TaskMode mode, URL target, int flags) throws NotImplementedException {
        if (this instanceof FileImpl) {
            return new FileCopyTask<NSEntry,Void>(mode, m_session, (FileImpl) this, target, flags);
        } else if (this instanceof DirectoryImpl) {
            return new DirectoryCopyTask<NSEntry,Void>(mode, m_session, (DirectoryImpl) this, target, flags);
        } else {
            return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                    mode, m_session, this,
                    "copySync",
                    new Class[]{URL.class, int.class},
                    new Object[]{target, flags});
        }
    }
    public Task<NSEntry, Void> copy(TaskMode mode, URL target) throws NotImplementedException {
        return this.copy(mode, target, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> copyFrom(TaskMode mode, URL source, int flags) throws NotImplementedException {
        if (this instanceof FileImpl) {
            return new FileCopyFromTask<NSEntry,Void>(mode, m_session, (FileImpl) this, source, flags);
        } else if (this instanceof DirectoryImpl) {
            return new DirectoryCopyFromTask<NSEntry,Void>(mode, m_session, (DirectoryImpl) this, source, flags);
        } else {
            return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                    mode, m_session, this,
                    "copyFromSync",
                    new Class[]{URL.class, int.class},
                    new Object[]{source, flags});
        }
    }
    public Task<NSEntry, Void> copyFrom(TaskMode mode, URL source) throws NotImplementedException {
        return this.copyFrom(mode, source, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> link(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "linkSync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }
    public Task<NSEntry, Void> link(TaskMode mode, URL target) throws NotImplementedException {
        return this.link(mode, target, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> move(TaskMode mode, URL target, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "moveSync",
                new Class[]{URL.class, int.class},
                new Object[]{target, flags});
    }
    public Task<NSEntry, Void> move(TaskMode mode, URL target) throws NotImplementedException {
        return this.move(mode, target, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> remove(TaskMode mode, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "removeSync",
                new Class[]{int.class},
                new Object[]{flags});
    }
    public Task<NSEntry, Void> remove(TaskMode mode) throws NotImplementedException {
        return this.remove(mode, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> close(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "close",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, Void> close(TaskMode mode, float timeoutInSeconds) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "close",
                new Class[]{float.class},
                new Object[]{timeoutInSeconds});
    }

    public Task<NSEntry, Void> permissionsAllow(TaskMode mode, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "permissionsAllowSync",
                new Class[]{String.class, int.class, int.class},
                new Object[]{id, permissions, flags});
    }

    public Task<NSEntry, Void> permissionsDeny(TaskMode mode, String id, int permissions, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, this,
                "permissionsDenySync",
                new Class[]{String.class, int.class, int.class},
                new Object[]{id, permissions, flags});
    }
}
