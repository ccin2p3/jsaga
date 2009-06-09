package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncLogicalFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalFileFactoryImpl extends AbstractSyncLogicalFileFactoryImpl {
    public AbstractAsyncLogicalFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected Task<LogicalFileFactory, LogicalFile> doCreateLogicalFile(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFileFactory,LogicalFile>(mode) {
            public LogicalFile invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalFileFactoryImpl.super.doCreateLogicalFileSync(session, name, flags);
            }
        };
    }

    protected Task<LogicalFileFactory, LogicalDirectory> doCreateLogicalDirectory(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<LogicalFileFactory,LogicalDirectory>(mode) {
            public LogicalDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncLogicalFileFactoryImpl.super.doCreateLogicalDirectorySync(session, name, flags);
            }
        };
    }
}
