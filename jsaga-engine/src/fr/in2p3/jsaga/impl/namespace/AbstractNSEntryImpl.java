package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryImpl extends AbstractAsyncNSEntryImpl implements NSEntry {
    /** constructor for factory */
    protected AbstractNSEntryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    //////////////////////////////////////////// interface NSEntry ////////////////////////////////////////////

    public URL getURL() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getURL");
        if (timeout == WAIT_FOREVER) {
            return super.getURLSync();
        } else {
            try {
                return (URL) getResult(super.getURL(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public URL getCWD() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getCWD");
        if (timeout == WAIT_FOREVER) {
            return super.getCWDSync();
        } else {
            try {
                return (URL) getResult(super.getCWD(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public URL getName() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getName");
        if (timeout == WAIT_FOREVER) {
            return super.getNameSync();
        } else {
            try {
                return (URL) getResult(super.getName(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isDir() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isDir");
        if (timeout == WAIT_FOREVER) {
            return super.isDirSync();
        } else {
            try {
                return (Boolean) getResult(super.isDir(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isEntry() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isEntry");
        if (timeout == WAIT_FOREVER) {
            return super.isEntrySync();
        } else {
            try {
                return (Boolean) getResult(super.isEntry(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isLink() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isLink");
        if (timeout == WAIT_FOREVER) {
            return super.isLinkSync();
        } else {
            try {
                return (Boolean) getResult(super.isLink(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public URL readLink() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("readLink");
        if (timeout == WAIT_FOREVER) {
            return super.readLinkSync();
        } else {
            try {
                return (URL) getResult(super.readLink(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public long getMTime() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getMTime");
        if (timeout == WAIT_FOREVER) {
            return super.getMTimeSync();
        } else {
            try {
                return (Long) getResult(super.getMTime(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            // implemented by inheriting classes
            this.copySync(target, flags);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(target);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copyFrom");
        if (timeout == WAIT_FOREVER) {
            // implemented by inheriting classes
            this.copyFromSync(source, flags);
        } else {
            try {
                getResult(super.copyFrom(TaskMode.ASYNC, source, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copyFrom(URL source) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copyFrom");
        if (timeout == WAIT_FOREVER) {
            super.copyFromSync(source);
        } else {
            try {
                getResult(super.copyFrom(TaskMode.ASYNC, source), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(target, flags);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(target);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(target, flags);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(target);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync(flags);
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC, flags), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync();
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void close() throws NotImplementedException, NoSuccessException {
        float timeout = this.getTimeout("close");
        if (timeout == WAIT_FOREVER) {
            super.close();
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: close");
        }
    }

    public void close(float timeoutInSeconds) throws NotImplementedException, NoSuccessException {
        float timeout = this.getTimeout("close");
        if (timeout == WAIT_FOREVER) {
            super.close(timeoutInSeconds);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: close");
        }
    }

    public void permissionsAllow(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsAllow");
        if (timeout == WAIT_FOREVER) {
            super.permissionsAllowSync(id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsAllow(TaskMode.ASYNC, id, permissions, flags), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsDeny(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectStateException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsDeny");
        if (timeout == WAIT_FOREVER) {
            super.permissionsDenySync(id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsDeny(TaskMode.ASYNC, id, permissions, flags), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(NSEntry.class, methodName, m_url.getScheme());
    }
}
