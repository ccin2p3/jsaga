package fr.in2p3.jsaga.impl.resource;

import fr.in2p3.jsaga.engine.factories.ResourceAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceFactoryImpl extends AbstractAsyncResourceFactoryImpl {
    /** constructor */
    public ResourceFactoryImpl(ResourceAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected ResourceManager doCreateResourceManager(Session session, URL rm) throws NotImplementedException, BadParameterException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createResourceManager", rm);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateManagerSync(session, rm);
        } else {
            try {
                return (ResourceManager) this.getResult(super.doCreateResourceManager(TaskMode.ASYNC, session, rm), timeout);
            } catch (PermissionDeniedException | IncorrectStateException
                    | AlreadyExistsException | DoesNotExistException
                    | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName, URL rm) throws NoSuccessException {
        return AbstractSagaObjectImpl.getTimeout(JobFactory.class, methodName, rm.getScheme());
    }

    private Object getResult(Task task, float timeout)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException,
            TimeoutException, NoSuccessException, SagaIOException
    {
        return AbstractSagaObjectImpl.getResult(task, timeout);
    }
}
