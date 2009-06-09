package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.io.IOException;
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

    public Task<FileOutputStream, Void> write(TaskMode mode, final int b) throws NotImplementedException {
        return new AbstractThreadedTask<FileOutputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileOutputStreamImpl.this.write(b);
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileOutputStream, Void> write(TaskMode mode, final byte[] buf, final int off, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<FileOutputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileOutputStreamImpl.this.write(buf, off, len);
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileOutputStream, Void> flush(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileOutputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileOutputStreamImpl.this.flush();
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileOutputStream, Void> close(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileOutputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileOutputStreamImpl.this.close();
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
}
