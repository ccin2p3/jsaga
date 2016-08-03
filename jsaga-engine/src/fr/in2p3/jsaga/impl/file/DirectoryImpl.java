package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryImpl extends AbstractAsyncDirectoryImpl implements Directory {
    /** constructor for factory */
    public DirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public DirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public DirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////////// interface Directory //////////////////////////////////////////

    // <extra specs>
    public long getSize() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getSize");
        if (timeout == WAIT_FOREVER) {
            return super.getSizeSync();
        } else {
            try {
                return (Long) getResult(super.getSize(TaskMode.ASYNC), timeout);
            } catch (AlreadyExistsException | IncorrectURLException | SagaIOException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }
    
    public long getSize(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getSize");
        if (timeout == WAIT_FOREVER) {
            return super.getSizeSync(flags);
        } else {
            try {
                return (Long) getResult(super.getSize(TaskMode.ASYNC, flags), timeout);
            } catch (AlreadyExistsException | IncorrectURLException | SagaIOException | DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }
    // </extra specs>

    public long getSize(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getSize");
        if (timeout == WAIT_FOREVER) {
            return super.getSizeSync(name, flags);
        } else {
            try {
                return (Long) getResult(super.getSize(TaskMode.ASYNC, name, flags), timeout);
            } catch (AlreadyExistsException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public long getSize(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getSize");
        if (timeout == WAIT_FOREVER) {
            return super.getSizeSync(name);
        } else {
            try {
                return (Long) getResult(super.getSize(TaskMode.ASYNC, name), timeout);
            } catch (AlreadyExistsException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public boolean isFile(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isFile");
        if (timeout == WAIT_FOREVER) {
            return super.isFileSync(name);
        } else {
            try {
                return (Boolean) getResult(super.isFile(TaskMode.ASYNC, name), timeout);
            } catch (AlreadyExistsException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    public Directory openDirectory(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openDirectory");
        if (timeout == WAIT_FOREVER) {
            return super.openDirectory(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openDirectory");
        }
    }

    public Directory openDirectory(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openDirectory");
        if (timeout == WAIT_FOREVER) {
            return super.openDirectory(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openDirectory");
        }
    }

    public File openFile(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openFile");
        if (timeout == WAIT_FOREVER) {
            return super.openFile(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openFile");
        }
    }

    public File openFile(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openFile");
        if (timeout == WAIT_FOREVER) {
            return super.openFile(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openFile");
        }
    }

    public FileInputStream openFileInputStream(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openFileInputStream");
        if (timeout == WAIT_FOREVER) {
            return super.openFileInputStreamSync(name);
        } else {
            try {
                return (FileInputStream) getResult(super.openFileInputStream(TaskMode.ASYNC, name), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public FileOutputStream openFileOutputStream(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openFileOutputStream");
        if (timeout == WAIT_FOREVER) {
            return super.openFileOutputStreamSync(name);
        } else {
            try {
                return (FileOutputStream) getResult(super.openFileOutputStream(TaskMode.ASYNC, name), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public FileOutputStream openFileOutputStream(URL name, boolean append) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openFileOutputStream");
        if (timeout == WAIT_FOREVER) {
            return super.openFileOutputStreamSync(name, append);
        } else {
            try {
                return (FileOutputStream) getResult(super.openFileOutputStream(TaskMode.ASYNC, name, append), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(Directory.class, methodName, m_url.getScheme());
    }
}
