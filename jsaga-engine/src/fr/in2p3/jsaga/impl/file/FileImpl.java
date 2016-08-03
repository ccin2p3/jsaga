package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileImpl extends AbstractAsyncFileImpl implements File {
    /** constructor for factory */
    public FileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public FileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public FileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    //////////////////////////////////////////// interface File ////////////////////////////////////////////

    public long getSize() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getSize");
        if (timeout == WAIT_FOREVER) {
            return super.getSizeSync();
        } else {
            try {
                return (Long) getResult(super.getSize(TaskMode.ASYNC), timeout);
            } catch (IncorrectURLException | BadParameterException | DoesNotExistException | SagaIOException | AlreadyExistsException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int read(Buffer buffer, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("read");
        if (timeout == WAIT_FOREVER) {
            return super.readSync(buffer, len);
        } else {
            try {
                return (Integer) getResult(super.read(TaskMode.ASYNC, buffer, len), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int read(Buffer buffer, int offset, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("read");
        if (timeout == WAIT_FOREVER) {
            return super.readSync(buffer, offset, len);
        } else {
            try {
                return (Integer) getResult(super.read(TaskMode.ASYNC, buffer, offset, len), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int read(Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("read");
        if (timeout == WAIT_FOREVER) {
            return super.readSync(buffer);
        } else {
            try {
                return (Integer) getResult(super.read(TaskMode.ASYNC, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int write(Buffer buffer, int offset, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("write");
        if (timeout == WAIT_FOREVER) {
            return super.writeSync(buffer, offset, len);
        } else {
            try {
                return (Integer) getResult(super.write(TaskMode.ASYNC, buffer, offset, len), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int write(Buffer buffer, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("write");
        if (timeout == WAIT_FOREVER) {
            return super.writeSync(buffer, len);
        } else {
            try {
                return (Integer) getResult(super.write(TaskMode.ASYNC, buffer, len), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int write(Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("write");
        if (timeout == WAIT_FOREVER) {
            return super.writeSync(buffer);
        } else {
            try {
                return (Integer) getResult(super.write(TaskMode.ASYNC, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public long seek(long offset, SeekMode whence) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("seek");
        if (timeout == WAIT_FOREVER) {
            return super.seekSync(offset, whence);
        } else {
            try {
                return (Long) getResult(super.seek(TaskMode.ASYNC, offset, whence), timeout);
            } catch (IncorrectURLException | BadParameterException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public void readV(IOVec[] iovecs) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("readV");
        if (timeout == WAIT_FOREVER) {
            super.readVSync(iovecs);
        } else {
            try {
                getResult(super.readV(TaskMode.ASYNC, iovecs), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public void writeV(IOVec[] iovecs) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("writeV");
        if (timeout == WAIT_FOREVER) {
            super.writeVSync(iovecs);
        } else {
            try {
                getResult(super.writeV(TaskMode.ASYNC, iovecs), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int sizeP(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectStateException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("sizeP");
        if (timeout == WAIT_FOREVER) {
            return super.sizePSync(pattern);
        } else {
            try {
                return (Integer) getResult(super.sizeP(TaskMode.ASYNC, pattern), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | SagaIOException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int readP(String pattern, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("readP");
        if (timeout == WAIT_FOREVER) {
            return super.readPSync(pattern, buffer);
        } else {
            try {
                return (Integer) getResult(super.readP(TaskMode.ASYNC, pattern, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int writeP(String pattern, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("writeP");
        if (timeout == WAIT_FOREVER) {
            return super.writePSync(pattern, buffer);
        } else {
            try {
                return (Integer) getResult(super.writeP(TaskMode.ASYNC, pattern, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public List<String> modesE() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("modesE");
        if (timeout == WAIT_FOREVER) {
            return super.modesESync();
        } else {
            try {
                return (List<String>) getResult(super.modesE(TaskMode.ASYNC), timeout);
            } catch (IncorrectURLException | BadParameterException | DoesNotExistException | AlreadyExistsException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int sizeE(String emode, String spec) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectStateException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("sizeE");
        if (timeout == WAIT_FOREVER) {
            return super.sizeESync(emode, spec);
        } else {
            try {
                return (Integer) getResult(super.sizeE(TaskMode.ASYNC, emode, spec), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | SagaIOException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int readE(String emode, String spec, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("readE");
        if (timeout == WAIT_FOREVER) {
            return super.readESync(emode, spec, buffer);
        } else {
            try {
                return (Integer) getResult(super.readE(TaskMode.ASYNC, emode, spec, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public int writeE(String emode, String spec, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        float timeout = this.getTimeout("writeE");
        if (timeout == WAIT_FOREVER) {
            return super.writeESync(emode, spec, buffer);
        } else {
            try {
                return (Integer) getResult(super.writeE(TaskMode.ASYNC, emode, spec, buffer), timeout);
            } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(File.class, methodName, m_url.getScheme());
    }
}
