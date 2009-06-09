package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
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
 * File:   AbstractAsyncJobFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   5 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncJobFactoryImpl extends AbstractSyncJobFactoryImpl {
    public AbstractAsyncJobFactoryImpl(JobAdaptorFactory adaptorFactory, JobMonitorAdaptorFactory monitorAdaptorFactory) {
        super(adaptorFactory, monitorAdaptorFactory);
    }

    protected Task<JobFactory, JobService> doCreateJobService(TaskMode mode, final Session session, final URL rm) throws NotImplementedException {
        return new AbstractThreadedTask<JobFactory,JobService>(mode) {
            public JobService invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobFactoryImpl.super.doCreateJobServiceSync(session, rm);
            }
        };
    }
}
