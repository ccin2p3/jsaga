package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncLogicalDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalDirectoryImpl extends AbstractSyncLogicalDirectoryImpl implements LogicalDirectory {
    /** constructor for factory */
    protected AbstractAsyncLogicalDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncLogicalDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncLogicalDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    /////////////////////////////////////// interface LogicalDirectory ///////////////////////////////////////

    public Task<NSDirectory, Boolean> isFile(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<NSDirectory,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.isFileSync(name);
            }
        };
    }

    public Task<LogicalDirectory, List<URL>> find(TaskMode mode, final String namePattern, final String[] attrPattern, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.findSync(namePattern, attrPattern, flags);
            }
        };
    }
    public Task<LogicalDirectory, List<URL>> find(TaskMode mode, final String namePattern, final String[] attrPattern) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.findSync(namePattern, attrPattern);
            }
        };
    }

    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,LogicalDirectory>(mode) {
            public LogicalDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.openLogicalDir(name, flags);
            }
        };
    }
    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,LogicalDirectory>(mode) {
            public LogicalDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.openLogicalDir(name);
            }
        };
    }

    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,LogicalFile>(mode) {
            public LogicalFile invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.openLogicalFile(name, flags);
            }
        };
    }
    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalDirectory,LogicalFile>(mode) {
            public LogicalFile invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalDirectoryImpl.super.openLogicalFile(name);
            }
        };
    }
}
