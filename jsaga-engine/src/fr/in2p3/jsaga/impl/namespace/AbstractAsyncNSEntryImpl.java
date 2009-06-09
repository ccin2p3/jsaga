package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.file.AbstractSyncDirectoryImpl;
import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;
import fr.in2p3.jsaga.impl.file.copy.*;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
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
        return new AbstractThreadedTask<NSEntry,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.getURLSync();
            }
        };
    }

    public Task<NSEntry, URL> getCWD(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.getCWDSync();
            }
        };
    }

    public Task<NSEntry, URL> getName(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.getNameSync();
            }
        };
    }

    public Task<NSEntry, Boolean> isDir(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.isDirSync();
            }
        };
    }

    public Task<NSEntry, Boolean> isEntry(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.isEntrySync();
            }
        };
    }

    public Task<NSEntry, Boolean> isLink(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.isLinkSync();
            }
        };
    }

    public Task<NSEntry, URL> readLink(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSEntryImpl.super.readLinkSync();
            }
        };
    }

    public Task<NSEntry, Void> copy(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        if (this instanceof AbstractSyncFileImpl) {
            return new FileCopyTask<NSEntry,Void>(mode, m_session, (AbstractSyncFileImpl) this, target, flags);
        } else if (this instanceof AbstractSyncDirectoryImpl) {
            return new DirectoryCopyTask<NSEntry,Void>(mode, m_session, (AbstractSyncDirectoryImpl) this, target, flags);
        } else {
            return new AbstractThreadedTask<NSEntry,Void>(mode) {
                public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                    AbstractAsyncNSEntryImpl.this.copySync(target, flags);
                    return null;
                }
            };
        }
    }
    public Task<NSEntry, Void> copy(TaskMode mode, final URL target) throws NotImplementedException {
        return this.copy(mode, target, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> copyFrom(TaskMode mode, final URL source, final int flags) throws NotImplementedException {
        if (this instanceof AbstractSyncFileImpl) {
            return new FileCopyFromTask<NSEntry,Void>(mode, m_session, (AbstractSyncFileImpl) this, source, flags);
        } else if (this instanceof AbstractSyncDirectoryImpl) {
            return new DirectoryCopyFromTask<NSEntry,Void>(mode, m_session, (AbstractSyncDirectoryImpl) this, source, flags);
        } else {
            return new AbstractThreadedTask<NSEntry,Void>(mode) {
                public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                    AbstractAsyncNSEntryImpl.this.copyFromSync(source, flags);
                    return null;
                }
            };
        }
    }
    public Task<NSEntry, Void> copyFrom(TaskMode mode, final URL source) throws NotImplementedException {
        return this.copyFrom(mode, source, Flags.NONE.getValue());
    }

    public Task<NSEntry, Void> link(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.linkSync(target, flags);
                return null;
            }
        };
    }
    public Task<NSEntry, Void> link(TaskMode mode, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.linkSync(target);
                return null;
            }
        };
    }

    public Task<NSEntry, Void> move(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.moveSync(target, flags);
                return null;
            }
        };
    }
    public Task<NSEntry, Void> move(TaskMode mode, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.moveSync(target);
                return null;
            }
        };
    }

    public Task<NSEntry, Void> remove(TaskMode mode, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.removeSync(flags);
                return null;
            }
        };
    }
    public Task<NSEntry, Void> remove(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.removeSync();
                return null;
            }
        };
    }

    public Task<NSEntry, Void> close(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.close();
                return null;
            }
        };
    }

    public Task<NSEntry, Void> close(TaskMode mode, final float timeoutInSeconds) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.close(timeoutInSeconds);
                return null;
            }
        };
    }

    public Task<NSEntry, Void> permissionsAllow(TaskMode mode, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.permissionsAllowSync(id, permissions, flags);
                return null;
            }
        };
    }

    public Task<NSEntry, Void> permissionsDeny(TaskMode mode, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSEntryImpl.super.permissionsDenySync(id, permissions, flags);
                return null;
            }
        };
    }
}
