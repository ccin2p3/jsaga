package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSEntry;
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

    public Task<NSDirectory, Void> changeDir(TaskMode mode, final URL dir) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.changeDirSync(dir);
                return null;
            }
        };
    }

    public Task<NSDirectory, List<URL>> list(TaskMode mode, final String pattern, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.listSync(pattern, flags);
            }
        };
    }
    public Task<NSDirectory, List<URL>> list(TaskMode mode, final String pattern) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.listSync(pattern);
            }
        };
    }

    public Task<NSDirectory, List<URL>> list(TaskMode mode, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.listSync(flags);
            }
        };
    }
    public Task<NSDirectory, List<URL>> list(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.listSync();
            }
        };
    }

    public Task<NSDirectory, List<URL>> find(TaskMode mode, final String pattern, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.findSync(pattern, flags);
            }
        };
    }
    public Task<NSDirectory, List<URL>> find(TaskMode mode, final String pattern) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.findSync(pattern);
            }
        };
    }

    public Task<NSDirectory, Boolean> exists(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.existsSync(name);
            }
        };
    }

    public Task<NSDirectory, Boolean> isDir(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.isDirSync(name);
            }
        };
    }

    public Task<NSDirectory, Boolean> isEntry(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.isEntrySync(name);
            }
        };
    }

    public Task<NSDirectory, Boolean> isLink(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.isLinkSync(name);
            }
        };
    }

    public Task<NSDirectory, Long> getMTime(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.getMTimeSync(name);
            }
        };
    }

    public Task<NSDirectory, URL> readLink(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.readLinkSync(name);
            }
        };
    }

    public Task<NSDirectory, Integer> getNumEntries(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.getNumEntriesSync();
            }
        };
    }

    public Task<NSDirectory, URL> getEntry(TaskMode mode, final int entry) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.getEntrySync(entry);
            }
        };
    }

    public Task<NSDirectory, Void> copy(TaskMode mode, final URL source, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.copySync(source, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> copy(TaskMode mode, final URL source, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.copySync(source, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> copy(TaskMode mode, final String sourcePattern, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.copySync(sourcePattern, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> copy(TaskMode mode, final String sourcePattern, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.copySync(sourcePattern, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> link(TaskMode mode, final URL source, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.linkSync(source, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> link(TaskMode mode, final URL source, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.linkSync(source, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> link(TaskMode mode, final String sourcePattern, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.linkSync(sourcePattern, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> link(TaskMode mode, final String sourcePattern, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.linkSync(sourcePattern, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> move(TaskMode mode, final URL source, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.moveSync(source, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> move(TaskMode mode, final URL source, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.moveSync(source, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> move(TaskMode mode, final String sourcePattern, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.moveSync(sourcePattern, target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> move(TaskMode mode, final String sourcePattern, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.moveSync(sourcePattern, target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> remove(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.removeSync(target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> remove(TaskMode mode, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.removeSync(target);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> remove(TaskMode mode, final String targetPattern, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.removeSync(targetPattern, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> remove(TaskMode mode, final String targetPattern) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.removeSync(targetPattern);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> makeDir(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.makeDirSync(target, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> makeDir(TaskMode mode, final URL target) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.makeDirSync(target);
                return null;
            }
        };
    }

    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,NSDirectory>(mode) {
            public NSDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.this.openDir(name, flags);
            }
        };
    }
    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,NSDirectory>(mode) {
            public NSDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.this.openDir(name);
            }
        };
    }

    public Task<NSDirectory, NSEntry> open(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,NSEntry>(mode) {
            public NSEntry invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.this.open(name, flags);
            }
        };
    }
    public Task<NSDirectory, NSEntry> open(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,NSEntry>(mode) {
            public NSEntry invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.this.open(name);
            }
        };
    }

    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, final URL target, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsAllowSync(target, id, permissions, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, final URL target, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsAllowSync(target, id, permissions);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, final String targetPattern, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsAllowSync(targetPattern, id, permissions, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, final String targetPattern, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsAllowSync(targetPattern, id, permissions);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, final URL target, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsDenySync(target, id, permissions, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, final URL target, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsDenySync(target, id, permissions);
                return null;
            }
        };
    }

    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, final String targetPattern, final String id, final int permissions, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsDenySync(targetPattern, id, permissions, flags);
                return null;
            }
        };
    }
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, final String targetPattern, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.permissionsDenySync(targetPattern, id, permissions);
                return null;
            }
        };
    }

    /////////////////////////////////////// override some methods of NSEntry ///////////////////////////////////////

    /** override super.getCWD() */
    public Task<NSEntry, URL> getCWD(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,URL>(mode) {
            public URL invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSDirectoryImpl.super.getCWDSync();
            }
        };
    }

    /** override super.copy() */
    public Task<NSEntry, Void> copy(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.copySync(target, flags);
                return null;
            }
        };
    }

    /** override super.move() */
    public Task<NSEntry, Void> move(TaskMode mode, final URL target, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.moveSync(target, flags);
                return null;
            }
        };
    }

    /** override super.remove() */
    public Task<NSEntry, Void> remove(TaskMode mode, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncNSDirectoryImpl.super.removeSync(flags);
                return null;
            }
        };
    }
}
