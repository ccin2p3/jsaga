package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileInputStream;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.io.IOException;
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
        return new AbstractThreadedTask<FileInputStream,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileInputStreamImpl.this.read();
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Integer> read(TaskMode mode, final byte[] buf, final int off, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileInputStreamImpl.this.read(buf, off, len);
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Long> skip(TaskMode mode, final long n) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileInputStreamImpl.this.skip(n);
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Integer> available(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileInputStreamImpl.this.available();
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Void> close(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileInputStreamImpl.this.close();
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Void> mark(TaskMode mode, final int readlimit) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncFileInputStreamImpl.this.mark(readlimit);
                return null;
            }
        };
    }

    public Task<FileInputStream, Void> reset(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileInputStreamImpl.this.reset();
                    return null;
                } catch (IOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<FileInputStream, Boolean> markSupported(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<FileInputStream,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileInputStreamImpl.this.markSupported();
            }
        };
    }
}
