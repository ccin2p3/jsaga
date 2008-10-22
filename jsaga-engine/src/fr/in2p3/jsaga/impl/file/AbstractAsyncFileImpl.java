package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.url.URL;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileImpl extends AbstractNSEntryImplWithStream implements File {
    /** constructor for factory */
    public AbstractAsyncFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public AbstractAsyncFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractAsyncFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry, absolutePath, flags);
    }

    public Task<Long> getSize(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("getSize", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> read(TaskMode mode, Buffer buffer, int len) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("read", new Class[]{Buffer.class, int.class}),
                    new Object[]{buffer, len}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<Integer> read(TaskMode mode, Buffer buffer) throws NotImplemented {
        try {
            return this.read(mode, buffer, buffer.getSize());
        } catch (IncorrectState e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> write(TaskMode mode, Buffer buffer, int len) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("write", new Class[]{Buffer.class, int.class}),
                    new Object[]{buffer, len}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<Integer> write(TaskMode mode, Buffer buffer) throws NotImplemented {
        try {
            return this.write(mode, buffer, buffer.getSize());
        } catch (IncorrectState e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Long> seek(TaskMode mode, long offset, SeekMode whence) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("seek", new Class[]{long.class, SeekMode.class}),
                    new Object[]{offset, whence}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task readV(TaskMode mode, IOVec[] iovecs) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("readV", new Class[]{IOVec[].class}),
                    new Object[]{iovecs}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task writeV(TaskMode mode, IOVec[] iovecs) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("writeV", new Class[]{IOVec[].class}),
                    new Object[]{iovecs}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> sizeP(TaskMode mode, String pattern) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("sizeP", new Class[]{String.class}),
                    new Object[]{pattern}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> readP(TaskMode mode, String pattern, Buffer buffer) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("readP", new Class[]{String.class, Buffer.class}),
                    new Object[]{pattern, buffer}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> writeP(TaskMode mode, String pattern, Buffer buffer) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("writeP", new Class[]{String.class, Buffer.class}),
                    new Object[]{pattern, buffer}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<List<String>> modesE(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("modesE", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> sizeE(TaskMode mode, String emode, String spec) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("sizeE", new Class[]{String.class, String.class}),
                    new Object[]{emode, spec}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> readE(TaskMode mode, String emode, String spec, Buffer buffer) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("readE", new Class[]{String.class, String.class, Buffer.class}),
                    new Object[]{emode, spec, buffer}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Integer> writeE(TaskMode mode, String emode, String spec, Buffer buffer) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    FileImpl.class.getMethod("writeE", new Class[]{String.class, String.class, Buffer.class}),
                    new Object[]{emode, spec, buffer}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
