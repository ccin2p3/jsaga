package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.Flags;
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

    public Task<NSDirectory, Boolean> isFile(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSDirectory,Boolean>().create(
                mode, m_session, this,
                "isFileSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<LogicalDirectory, List<URL>> find(TaskMode mode, String namePattern, String[] attrPattern, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalDirectory,List<URL>>().create(
                mode, m_session, this,
                "findSync",
                new Class[]{String.class, String[].class, int.class},
                new Object[]{namePattern, attrPattern, flags});
    }
    public Task<LogicalDirectory, List<URL>> find(TaskMode mode, String namePattern, String[] attrPattern) throws NotImplementedException {
        return this.find(mode, namePattern, attrPattern, Flags.RECURSIVE.getValue());
    }

    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalDirectory,LogicalDirectory>().create(
                mode, m_session, this,
                "openLogicalDir",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(TaskMode mode, URL name) throws NotImplementedException {
        return this.openLogicalDir(mode, name, Flags.READ.getValue());
    }

    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalDirectory,LogicalFile>().create(
                mode, m_session, this,
                "openLogicalFile",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode, URL name) throws NotImplementedException {
        return this.openLogicalFile(mode, name, Flags.READ.getValue());
    }
}
