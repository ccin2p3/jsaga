package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryImpl extends AbstractAsyncNSDirectoryImpl implements NSDirectory {
    /** constructor for factory */
    protected AbstractNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////////// interface NSDirectory //////////////////////////////////////////

    public void changeDir(URL dir) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // can not hang...
        super.changeDirSync(dir);
    }

    public List<URL> list(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("list");
        if (timeout == WAIT_FOREVER) {
            return super.listSync(pattern, flags);
        } else {
            try {
                return (List<URL>) getResult(super.list(TaskMode.ASYNC, pattern, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public List<URL> list(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("list");
        if (timeout == WAIT_FOREVER) {
            return super.listSync(flags);
        } else {
            try {
                return (List<URL>) getResult(super.list(TaskMode.ASYNC, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public List<URL> list(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("list");
        if (timeout == WAIT_FOREVER) {
            return super.listSync(pattern);
        } else {
            try {
                return (List<URL>) getResult(super.list(TaskMode.ASYNC, pattern), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public List<URL> list() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("list");
        if (timeout == WAIT_FOREVER) {
            return super.listSync();
        } else {
            try {
                return (List<URL>) getResult(super.list(TaskMode.ASYNC), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public List<URL> find(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("find");
        if (timeout == WAIT_FOREVER) {
            return super.findSync(pattern, flags);
        } else {
            try {
                return (List<URL>) getResult(super.find(TaskMode.ASYNC, pattern, flags), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public List<URL> find(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("find");
        if (timeout == WAIT_FOREVER) {
            return super.findSync(pattern);
        } else {
            try {
                return (List<URL>) getResult(super.find(TaskMode.ASYNC, pattern), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean exists(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("exists");
        if (timeout == WAIT_FOREVER) {
            return super.existsSync(name);
        } else {
            try {
                return (Boolean) getResult(super.exists(TaskMode.ASYNC, name), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isDir(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isDir");
        if (timeout == WAIT_FOREVER) {
            return super.isDirSync(name);
        } else {
            try {
                return (Boolean) getResult(super.isDir(TaskMode.ASYNC, name), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isEntry(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isEntry");
        if (timeout == WAIT_FOREVER) {
            return super.isEntrySync(name);
        } else {
            try {
                return (Boolean) getResult(super.isEntry(TaskMode.ASYNC, name), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public boolean isLink(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("isLink");
        if (timeout == WAIT_FOREVER) {
            return super.isLinkSync(name);
        } else {
            try {
                return (Boolean) getResult(super.isLink(TaskMode.ASYNC, name), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public URL readLink(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("readLink");
        if (timeout == WAIT_FOREVER) {
            return super.readLinkSync(name);
        } else {
            try {
                return (URL) getResult(super.readLink(TaskMode.ASYNC, name), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public int getNumEntries() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getNumEntries");
        if (timeout == WAIT_FOREVER) {
            return super.getNumEntriesSync();
        } else {
            try {
                return (Integer) getResult(super.getNumEntries(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public URL getEntry(int entry) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getEntry");
        if (timeout == WAIT_FOREVER) {
            return super.getEntrySync(entry);
        } else {
            try {
                return (URL) getResult(super.getEntry(TaskMode.ASYNC, entry), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(source, target, flags);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(source, target);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(String source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(source, target, flags);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void copy(String source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(source, target);
        } else {
            try {
                getResult(super.copy(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(source, target, flags);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(source, target);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(String source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(source, target, flags);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void link(String source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("link");
        if (timeout == WAIT_FOREVER) {
            super.linkSync(source, target);
        } else {
            try {
                getResult(super.link(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(source, target, flags);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(source, target);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(String source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(source, target, flags);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, source, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void move(String source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("move");
        if (timeout == WAIT_FOREVER) {
            super.moveSync(source, target);
        } else {
            try {
                getResult(super.move(TaskMode.ASYNC, source, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync(target, flags);
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync(target);
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC, target), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove(String target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync(target, flags);
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void remove(String target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("remove");
        if (timeout == WAIT_FOREVER) {
            super.removeSync(target);
        } else {
            try {
                getResult(super.remove(TaskMode.ASYNC, target), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void makeDir(URL target, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("makeDir");
        if (timeout == WAIT_FOREVER) {
            super.makeDirSync(target, flags);
        } else {
            try {
                getResult(super.makeDir(TaskMode.ASYNC, target, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void makeDir(URL target) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("makeDir");
        if (timeout == WAIT_FOREVER) {
            super.makeDirSync(target);
        } else {
            try {
                getResult(super.makeDir(TaskMode.ASYNC, target), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public NSDirectory openDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openDir");
        if (timeout == WAIT_FOREVER) {
            return this.openDir(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openDir");
        }
    }

    public NSDirectory openDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openDir");
        if (timeout == WAIT_FOREVER) {
            return this.openDir(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openDir");
        }
    }

    public NSEntry open(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("open");
        if (timeout == WAIT_FOREVER) {
            return this.open(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: open");
        }
    }

    public NSEntry open(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("open");
        if (timeout == WAIT_FOREVER) {
            return this.open(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: open");
        }
    }

    public void permissionsAllow(URL target, String id, int permissions, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsAllow");
        if (timeout == WAIT_FOREVER) {
            super.permissionsAllowSync(target, id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsAllow(TaskMode.ASYNC, target, id, permissions, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsAllow(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsAllow");
        if (timeout == WAIT_FOREVER) {
            super.permissionsAllowSync(target, id, permissions);
        } else {
            try {
                getResult(super.permissionsAllow(TaskMode.ASYNC, target, id, permissions), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsAllow(String target, String id, int permissions, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsAllow");
        if (timeout == WAIT_FOREVER) {
            super.permissionsAllowSync(target, id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsAllow(TaskMode.ASYNC, target, id, permissions, flags), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsAllow(String target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsAllow");
        if (timeout == WAIT_FOREVER) {
            super.permissionsAllowSync(target, id, permissions);
        } else {
            try {
                getResult(super.permissionsAllow(TaskMode.ASYNC, target, id, permissions), timeout);
            }
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsDeny(URL target, String id, int permissions, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsDeny");
        if (timeout == WAIT_FOREVER) {
            super.permissionsDenySync(target, id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsDeny(TaskMode.ASYNC, target, id, permissions, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsDeny(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsDeny");
        if (timeout == WAIT_FOREVER) {
            super.permissionsDenySync(target, id, permissions);
        } else {
            try {
                getResult(super.permissionsDeny(TaskMode.ASYNC, target, id, permissions), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsDeny(String target, String id, int permissions, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsDeny");
        if (timeout == WAIT_FOREVER) {
            super.permissionsDenySync(target, id, permissions, flags);
        } else {
            try {
                getResult(super.permissionsDeny(TaskMode.ASYNC, target, id, permissions, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    public void permissionsDeny(String target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("permissionsDeny");
        if (timeout == WAIT_FOREVER) {
            super.permissionsDenySync(target, id, permissions);
        } else {
            try {
                getResult(super.permissionsDeny(TaskMode.ASYNC, target, id, permissions), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    /////////////////////////////////////// override some methods of NSEntry ///////////////////////////////////////

    /** override super.getCWD() */
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

    /** override super.copy() */
    public void copy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copy");
        if (timeout == WAIT_FOREVER) {
            super.copySync(target, flags);
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

    /** override super.copyFrom() */
    public void copyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        float timeout = this.getTimeout("copyFrom");
        if (timeout == WAIT_FOREVER) {
            super.copyFromSync(source, flags);
        } else {
            try {
                getResult(super.copyFrom(TaskMode.ASYNC, source, flags), timeout);
            }
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public void copyFrom(URL source) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
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

    /** override super.move() */
    public void move(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
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

    /** override super.remove() */
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

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(NSDirectory.class, methodName, m_url.getScheme());
    }
}
