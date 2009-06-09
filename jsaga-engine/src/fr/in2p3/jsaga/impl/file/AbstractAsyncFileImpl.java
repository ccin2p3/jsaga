package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
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
public abstract class AbstractAsyncFileImpl extends AbstractSyncFileImpl implements File {
    /** constructor for factory */
    protected AbstractAsyncFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    //////////////////////////////////////////// interface File ////////////////////////////////////////////

    public Task<File, Long> getSize(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<File,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileImpl.super.getSizeSync();
            }
        };
    }

    public Task<File, Integer> read(TaskMode mode, final Buffer buffer, final int offset, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.readSync(buffer, offset, len);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
    public Task<File, Integer> read(TaskMode mode, final Buffer buffer, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.readSync(buffer, len);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
    public Task<File, Integer> read(TaskMode mode, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.readSync(buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Integer> write(TaskMode mode, final Buffer buffer, final int offset, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.writeSync(buffer, offset, len);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
    public Task<File, Integer> write(TaskMode mode, final Buffer buffer, final int len) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.writeSync(buffer, len);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
    public Task<File, Integer> write(TaskMode mode, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.writeSync(buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Long> seek(TaskMode mode, final long offset, final SeekMode whence) throws NotImplementedException {
        return new AbstractThreadedTask<File,Long>(mode) {
            public Long invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.seekSync(offset, whence);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Void> readV(TaskMode mode, final IOVec[] iovecs) throws NotImplementedException {
        return new AbstractThreadedTask<File,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileImpl.super.readVSync(iovecs);
                    return null;
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Void> writeV(TaskMode mode, final IOVec[] iovecs) throws NotImplementedException {
        return new AbstractThreadedTask<File,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    AbstractAsyncFileImpl.super.writeVSync(iovecs);
                    return null;
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Integer> sizeP(TaskMode mode, final String pattern) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileImpl.super.sizePSync(pattern);
            }
        };
    }

    public Task<File, Integer> readP(TaskMode mode, final String pattern, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.readPSync(pattern, buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Integer> writeP(TaskMode mode, final String pattern, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.writePSync(pattern, buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, List<String>> modesE(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<File,List<String>>(mode) {
            public List<String> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileImpl.super.modesESync();
            }
        };
    }

    public Task<File, Integer> sizeE(TaskMode mode, final String emode, final String spec) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileImpl.super.sizeESync(emode, spec);
            }
        };
    }

    public Task<File, Integer> readE(TaskMode mode, final String emode, final String spec, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.readESync(emode, spec, buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }

    public Task<File, Integer> writeE(TaskMode mode, final String emode, final String spec, final Buffer buffer) throws NotImplementedException {
        return new AbstractThreadedTask<File,Integer>(mode) {
            public Integer invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                try {
                    return AbstractAsyncFileImpl.super.writeESync(emode, spec, buffer);
                } catch (SagaIOException e) {
                    throw new NoSuccessException(e);
                }
            }
        };
    }
}
