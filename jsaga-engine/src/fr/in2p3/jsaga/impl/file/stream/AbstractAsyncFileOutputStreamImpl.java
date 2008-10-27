package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncFileOutputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileOutputStreamImpl extends FileOutputStream {
    private Session m_session;
    private UUID m_uuid;

    /** constructor */
    public AbstractAsyncFileOutputStreamImpl(Session session) {
        m_session = session;
        m_uuid = UUID.randomUUID();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractAsyncFileOutputStreamImpl clone = (AbstractAsyncFileOutputStreamImpl) super.clone();
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

    public Task<FileOutputStream, Void> write(TaskMode mode, int b) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileOutputStream,Void>().create(
                mode, m_session, this,
                "write",
                new Class[]{int.class},
                new Object[]{b});
    }

    public Task<FileOutputStream, Void> write(TaskMode mode, byte[] buf, int off, int len) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileOutputStream,Void>().create(
                mode, m_session, this,
                "write",
                new Class[]{byte[].class, int.class, int.class},
                new Object[]{buf, off, len});
    }

    public Task<FileOutputStream, Void> flush(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileOutputStream,Void>().create(
                mode, m_session, this,
                "flush",
                new Class[]{},
                new Object[]{});
    }

    public Task<FileOutputStream, Void> close(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileOutputStream,Void>().create(
                mode, m_session, this,
                "close",
                new Class[]{},
                new Object[]{});
    }
}
