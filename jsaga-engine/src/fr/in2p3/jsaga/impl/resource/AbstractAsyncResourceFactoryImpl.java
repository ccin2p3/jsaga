package fr.in2p3.jsaga.impl.resource;

import fr.in2p3.jsaga.engine.factories.ResourceAdaptorFactory;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractAsyncResourceFactoryImpl extends AbstractSyncResourceFactoryImpl {
    /** constructor */
    public AbstractAsyncResourceFactoryImpl(ResourceAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected Task<ResourceFactory, ResourceManager> doCreateResourceManager(TaskMode mode, final Session session, final URL rm) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceFactory,ResourceManager>(mode) {
            public ResourceManager invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceFactoryImpl.super.doCreateManagerSync(session, rm);
            }
        };
    }
}
