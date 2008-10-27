package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
    protected AbstractAsyncFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    public Task<File, Long> getSize(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Long>().create(
                mode, m_session, this,
                "getSize",
                new Class[]{},
                new Object[]{});
    }

    public Task<File, Integer> read(TaskMode mode, Buffer buffer, int offset, int len) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "read",
                new Class[]{Buffer.class, int.class, int.class},
                new Object[]{buffer, offset, len});
    }
    public Task<File, Integer> read(TaskMode mode, Buffer buffer, int len) throws NotImplementedException {
        return this.read(mode, buffer, 0, len);
    }
    public Task<File, Integer> read(TaskMode mode, Buffer buffer) throws NotImplementedException {
        int len; try{len=buffer.getSize();} catch(IncorrectStateException e) {throw new NotImplementedException(e);}
        return this.read(mode, buffer, 0, len);
    }

    public Task<File, Integer> write(TaskMode mode, Buffer buffer, int offset, int len) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "write",
                new Class[]{Buffer.class, int.class, int.class},
                new Object[]{buffer, offset, len});
    }
    public Task<File, Integer> write(TaskMode mode, Buffer buffer, int len) throws NotImplementedException {
        return this.write(mode, buffer, 0, len);
    }
    public Task<File, Integer> write(TaskMode mode, Buffer buffer) throws NotImplementedException {
        int len; try{len=buffer.getSize();} catch(IncorrectStateException e) {throw new NotImplementedException(e);}
        return this.write(mode, buffer, 0, len);
    }

    public Task<File, Long> seek(TaskMode mode, long offset, SeekMode whence) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Long>().create(
                mode, m_session, this,
                "seek",
                new Class[]{long.class, SeekMode.class},
                new Object[]{offset, whence});
    }

    public Task<File, Void> readV(TaskMode mode, IOVec[] iovecs) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Void>().create(
                mode, m_session, this,
                "readV",
                new Class[]{IOVec[].class},
                new Object[]{iovecs});
    }

    public Task<File, Void> writeV(TaskMode mode, IOVec[] iovecs) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Void>().create(
                mode, m_session, this,
                "writeV",
                new Class[]{IOVec[].class},
                new Object[]{iovecs});
    }

    public Task<File, Integer> sizeP(TaskMode mode, String pattern) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "sizeP",
                new Class[]{String.class},
                new Object[]{pattern});
    }

    public Task<File, Integer> readP(TaskMode mode, String pattern, Buffer buffer) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "readP",
                new Class[]{String.class, Buffer.class},
                new Object[]{pattern, buffer});
    }

    public Task<File, Integer> writeP(TaskMode mode, String pattern, Buffer buffer) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "writeP",
                new Class[]{String.class, Buffer.class},
                new Object[]{pattern, buffer});
    }

    public Task<File, List<String>> modesE(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,List<String>>().create(
                mode, m_session, this,
                "modesE",
                new Class[]{},
                new Object[]{});
    }

    public Task<File, Integer> sizeE(TaskMode mode, String emode, String spec) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "sizeE",
                new Class[]{String.class, String.class},
                new Object[]{emode, spec});
    }

    public Task<File, Integer> readE(TaskMode mode, String emode, String spec, Buffer buffer) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "readE",
                new Class[]{String.class, String.class, Buffer.class},
                new Object[]{emode, spec, buffer});
    }

    public Task<File, Integer> writeE(TaskMode mode, String emode, String spec, Buffer buffer) throws NotImplementedException {
        return new GenericThreadedTaskFactory<File,Integer>().create(
                mode, m_session, this,
                "writeE",
                new Class[]{String.class, String.class, Buffer.class},
                new Object[]{emode, spec, buffer});
    }
}
