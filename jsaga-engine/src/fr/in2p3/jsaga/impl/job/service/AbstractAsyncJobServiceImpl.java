package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncJobServiceImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncJobServiceImpl extends AbstractSyncJobServiceImpl implements JobService {
    /** constructor */
    public AbstractAsyncJobServiceImpl(Session session, URL rm, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) {
        super(session, rm, controlAdaptor, monitorService);
    }

    public Task<JobService, Job> createJob(TaskMode mode, final JobDescription jd) throws NotImplementedException {
        return new AbstractThreadedTask<JobService,Job>(mode) {
            public Job invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobServiceImpl.super.createJobSync(jd);
            }
        };
    }

    public Task<JobService, List<String>> list(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<JobService,List<String>>(mode) {
            public List<String> invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobServiceImpl.super.listSync();
            }
        };
    }

    public Task<JobService, Job> getJob(TaskMode mode, final String jobId) throws NotImplementedException {
        return new AbstractThreadedTask<JobService,Job>(mode) {
            public Job invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobServiceImpl.super.getJobSync(jobId);
            }
        };
    }

    public Task<JobService, JobSelf> getSelf(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<JobService,JobSelf>(mode) {
            public JobSelf invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobServiceImpl.super.getSelfSync();
            }
        };
    }

    public Task<JobService, Job> runJob(TaskMode mode, final String commandLine, final String host, final boolean interactive) throws NotImplementedException {
        return new AbstractThreadedTask<JobService,Job>(mode) {
            public Job invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobServiceImpl.super.runJobSync(commandLine, host, interactive);
            }
        };
    }
    public Task<JobService, Job> runJob(TaskMode mode, final String commandLine, final String host) throws NotImplementedException {
        return this.runJob(mode, commandLine, host, DEFAULT_INTERACTIVE);
    }
    public Task<JobService, Job> runJob(TaskMode mode, final String commandLine, final boolean interactive) throws NotImplementedException {
        return this.runJob(mode, commandLine, DEFAULT_HOST, interactive);
    }
    public Task<JobService, Job> runJob(TaskMode mode, final String commandLine) throws NotImplementedException {
        return this.runJob(mode, commandLine, DEFAULT_HOST, DEFAULT_INTERACTIVE);
    }
}
