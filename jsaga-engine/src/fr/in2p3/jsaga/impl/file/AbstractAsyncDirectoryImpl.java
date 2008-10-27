package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.Flags;
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
public abstract class AbstractAsyncDirectoryImpl extends AbstractNSDirectoryImpl implements Directory {
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

    public Task<Directory, Long> getSize(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,Long>().create(
                mode, m_session, this,
                "getSize",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<Directory, Long> getSize(TaskMode mode, URL name) throws NotImplementedException {
        return this.getSize(mode, name, Flags.NONE.getValue());
    }

    public Task<Directory, Boolean> isFile(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,Boolean>().create(
                mode, m_session, this,
                "isFile",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<Directory, Directory> openDirectory(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,Directory>().create(
                mode, m_session, this,
                "openDirectory",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<Directory, Directory> openDirectory(TaskMode mode, URL name) throws NotImplementedException {
        return this.openDirectory(mode, name, Flags.READ.getValue());
    }

    public Task<Directory, File> openFile(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,File>().create(
                mode, m_session, this,
                "openFile",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<Directory, File> openFile(TaskMode mode, URL name) throws NotImplementedException {
        return this.openFile(mode, name, Flags.READ.getValue());
    }

    public Task<Directory, FileInputStream> openFileInputStream(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,FileInputStream>().create(
                mode, m_session, this,
                "openFileInputStream",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<Directory, FileOutputStream> openFileOutputStream(TaskMode mode, URL name, boolean append) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Directory,FileOutputStream>().create(
                mode, m_session, this,
                "openFileOutputStream",
                new Class[]{URL.class, boolean.class},
                new Object[]{name, append});
    }
    public Task<Directory, FileOutputStream> openFileOutputStream(TaskMode mode, URL name) throws NotImplementedException {
        return this.openFileOutputStream(mode, name, false);
    }
}
