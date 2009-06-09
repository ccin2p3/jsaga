package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncLogicalFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalFileImpl extends AbstractSyncLogicalFileImpl implements LogicalFile {
    /** constructor for factory */
    protected AbstractAsyncLogicalFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractAsyncLogicalFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractAsyncLogicalFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    //////////////////////////////////////// interface LogicalFile ////////////////////////////////////////

    public Task<LogicalFile, Void> addLocation(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncLogicalFileImpl.super.addLocationSync(name);
                return null;
            }
        };
    }

    public Task<LogicalFile, Void> removeLocation(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncLogicalFileImpl.super.removeLocationSync(name);
                return null;
            }
        };
    }

    public Task<LogicalFile, Void> updateLocation(TaskMode mode, final URL nameOld, final URL nameNew) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncLogicalFileImpl.super.updateLocationSync(nameOld, nameNew);
                return null;
            }
        };
    }

    public Task<LogicalFile, List<URL>> listLocations(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,List<URL>>(mode) {
            public List<URL> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalFileImpl.super.listLocationsSync();
            }
        };
    }

    public Task<LogicalFile, Void> replicate(TaskMode mode, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncLogicalFileImpl.super.replicateSync(name, flags);
                return null;
            }
        };
    }
    public Task<LogicalFile, Void> replicate(TaskMode mode, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFile,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncLogicalFileImpl.super.replicateSync(name);
                return null;
            }
        };
    }
}
