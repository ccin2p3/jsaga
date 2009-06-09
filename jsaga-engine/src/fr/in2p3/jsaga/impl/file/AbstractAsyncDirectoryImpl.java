package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncDirectoryImpl extends AbstractSyncDirectoryImpl implements Directory {
    /** constructor for factory */
    protected AbstractAsyncDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////////// interface Directory //////////////////////////////////////////

    public Task<Directory, Long> getSize(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.getSizeSync(name, flags);
            }
        };
    }
    public Task<Directory, Long> getSize(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.getSizeSync(name);
            }
        };
    }

    public Task<Directory, Boolean> isFile(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.isFileSync(name);
            }
        };
    }

    public Task<Directory, Directory> openDirectory(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,Directory>(mode) {
            public Directory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openDirectory(name, flags);
            }
        };
    }
    public Task<Directory, Directory> openDirectory(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,Directory>(mode) {
            public Directory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openDirectory(name);
            }
        };
    }

    public Task<Directory, File> openFile(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,File>(mode) {
            public File invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openFile(name, flags);
            }
        };
    }
    public Task<Directory, File> openFile(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,File>(mode) {
            public File invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openFile(name);
            }
        };
    }

    public Task<Directory, FileInputStream> openFileInputStream(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,FileInputStream>(mode) {
            public FileInputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openFileInputStreamSync(name);
            }
        };
    }

    public Task<Directory, FileOutputStream> openFileOutputStream(TaskMode mode, final URL name, final boolean append) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,FileOutputStream>(mode) {
            public FileOutputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openFileOutputStreamSync(name, append);
            }
        };
    }
    public Task<Directory, FileOutputStream> openFileOutputStream(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<Directory,FileOutputStream>(mode) {
            public FileOutputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncDirectoryImpl.super.openFileOutputStreamSync(name);
            }
        };
    }
}
