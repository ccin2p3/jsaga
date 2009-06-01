package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.Flags;
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

    public Task<LogicalFile, Void> addLocation(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "addLocationSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<LogicalFile, Void> removeLocation(TaskMode mode, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "removeLocationSync",
                new Class[]{URL.class},
                new Object[]{name});
    }

    public Task<LogicalFile, Void> updateLocation(TaskMode mode, URL nameOld, URL nameNew) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "updateLocationSync",
                new Class[]{URL.class, URL.class},
                new Object[]{nameOld, nameNew});
    }

    public Task<LogicalFile, List<URL>> listLocations(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,List<URL>>().create(
                mode, m_session, this,
                "listLocationsSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<LogicalFile, Void> replicate(TaskMode mode, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "replicateSync",
                new Class[]{URL.class, int.class},
                new Object[]{name, flags});
    }
    public Task<LogicalFile, Void> replicate(TaskMode mode, URL name) throws NotImplementedException {
        return this.replicate(mode, name, Flags.NONE.getValue());
    }
}
