package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobFactoryImpl extends AbstractAsyncJobFactoryImpl {
    public JobFactoryImpl(JobAdaptorFactory adaptorFactory, JobMonitorAdaptorFactory monitorAdaptorFactory) {
        super(adaptorFactory, monitorAdaptorFactory);
    }

    protected JobService doCreateJobService(Session session, URL rm) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createJobService", rm);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return this.doCreateJobServiceSync(session, rm);
        } else {
            try {
                return (JobService) this.getResult(createJobService(TaskMode.ASYNC, session, rm), timeout);
            }
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
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
            TimeoutException, NoSuccessException
    {
        return AbstractSagaObjectImpl.getResult(task, timeout);
    }
}
