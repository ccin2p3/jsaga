package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileInputStream;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncFileInputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileInputStreamImpl extends FileInputStream {
    private Session m_session;
    private UUID m_uuid;

    /** constructor */
    public AbstractAsyncFileInputStreamImpl(Session session) {
        m_session = session;
        m_uuid = UUID.randomUUID();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractAsyncFileInputStreamImpl clone = (AbstractAsyncFileInputStreamImpl) super.clone();
        clone.m_session = m_session;
        clone.m_uuid = UUID.randomUUID();
        return clone;
    }

    /////////////////////////////////// interface SagaObject ////////////////////////////////////

    public Session getSession() throws DoesNotExistException {
        if (m_session != null) {
            return m_session;
        } else {
            throw new DoesNotExistException("This object does not have a session attached", this);
        }
    }

    public String getId() {
        return m_uuid.toString();
    }

    ///////////////////////////////// interface FileInputStream /////////////////////////////////

    public Task<FileInputStream, Integer> read(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Integer>().create(
                mode, m_session, this,
                "read",
                new Class[]{},
                new Object[]{});
    }

    public Task<FileInputStream, Integer> read(TaskMode mode, byte[] buf, int off, int len) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Integer>().create(
                mode, m_session, this,
                "read",
                new Class[]{byte[].class, int.class, int.class},
                new Object[]{buf, off, len});
    }

    public Task<FileInputStream, Long> skip(TaskMode mode, long n) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Long>().create(
                mode, m_session, this,
                "skip",
                new Class[]{long.class},
                new Object[]{n});
    }

    public Task<FileInputStream, Integer> available(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Integer>().create(
                mode, m_session, this,
                "available",
                new Class[]{},
                new Object[]{});
    }

    public Task<FileInputStream, Void> close(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Void>().create(
                mode, m_session, this,
                "close",
                new Class[]{},
                new Object[]{});
    }

    public Task<FileInputStream, Void> mark(TaskMode mode, int readlimit) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Void>().create(
                mode, m_session, this,
                "mark",
                new Class[]{int.class},
                new Object[]{readlimit});
    }

    public Task<FileInputStream, Void> reset(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Void>().create(
                mode, m_session, this,
                "reset",
                new Class[]{},
                new Object[]{});
    }

    public Task<FileInputStream, Boolean> markSupported(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileInputStream,Boolean>().create(
                mode, m_session, this,
                "markSupported",
                new Class[]{},
                new Object[]{});
    }
}
