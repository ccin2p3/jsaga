package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.NotImplementedException;
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

    public Task<JobService, Job> createJob(TaskMode mode, JobDescription jd) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "createJobSync",
                new Class[]{JobDescription.class},
                new Object[]{jd});
    }

    public Task<JobService, List<String>> list(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,List<String>>().create(
                mode, m_session, this,
                "listSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<JobService, Job> getJob(TaskMode mode, String jobId) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "getJobSync",
                new Class[]{String.class},
                new Object[]{jobId});
    }

    public Task<JobService, JobSelf> getSelf(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,JobSelf>().create(
                mode, m_session, this,
                "getSelfSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, String host, boolean interactive) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "runJobSync",
                new Class[]{String.class, String.class, boolean.class},
                new Object[]{commandLine, host, interactive});
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, String host) throws NotImplementedException {
        return this.runJob(mode, commandLine, host, DEFAULT_INTERACTIVE);
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, boolean interactive) throws NotImplementedException {
        return this.runJob(mode, commandLine, DEFAULT_HOST, interactive);
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine) throws NotImplementedException {
        return this.runJob(mode, commandLine, DEFAULT_HOST, DEFAULT_INTERACTIVE);
    }
}
