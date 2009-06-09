package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncNSFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncNSFactoryImpl extends AbstractSyncNSFactoryImpl {
    public AbstractAsyncNSFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    ////////////////////////////////////////// interface NSFactory //////////////////////////////////////////

    protected Task<NSFactory, NSEntry> doCreateNSEntry(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSFactory,NSEntry>(mode) {
            public NSEntry invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSFactoryImpl.super.doCreateNSEntrySync(session, name, flags);
            }
        };
    }

    protected Task<NSFactory, NSDirectory> doCreateNSDirectory(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<NSFactory,NSDirectory>(mode) {
            public NSDirectory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncNSFactoryImpl.super.doCreateNSDirectorySync(session, name, flags);
            }
        };
    }
}
