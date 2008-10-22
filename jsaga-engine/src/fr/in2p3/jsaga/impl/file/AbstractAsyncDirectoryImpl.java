package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.url.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

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
    public AbstractAsyncDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public AbstractAsyncDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractAsyncDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, absolutePath, flags);
    }

    public Task<Long> getSize(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("getSize", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<Long> getSize(TaskMode mode, URL name) throws NotImplemented {
        return this.getSize(mode, name, Flags.NONE.getValue());
    }

    public Task<Boolean> isFile(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("isFile", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Directory> openDirectory(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("openDirectory", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<Directory> openDirectory(TaskMode mode, URL name) throws NotImplemented {
        return this.openDirectory(mode, name, Flags.READ.getValue());
    }

    public Task<File> openFile(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("openFile", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<File> openFile(TaskMode mode, URL name) throws NotImplemented {
        return this.openFile(mode, name, Flags.READ.getValue());
    }

    public Task<FileInputStream> openFileInputStream(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("openFileInputStream", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<FileOutputStream> openFileOutputStream(TaskMode mode, URL name) throws NotImplemented {
        return this.openFileOutputStream(mode, name, false);
    }
    public Task<FileOutputStream> openFileOutputStream(TaskMode mode, URL name, boolean append) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    DirectoryImpl.class.getMethod("openFileOutputStream", new Class[]{URL.class, boolean.class}),
                    new Object[]{name, append}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
