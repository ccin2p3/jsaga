package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.DataStagingDescription;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncJobImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncJobImpl extends AbstractSyncJobImpl implements Job {
    /** constructor for submission */
    protected AbstractAsyncJobImpl(Session session, String nativeJobDesc, JobDescription jobDesc, DataStagingDescription stagingDesc, String uniqId, AbstractSyncJobServiceImpl service) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, nativeJobDesc, jobDesc, stagingDesc, uniqId, service);
    }

    /** constructor for control and monitoring only */
    protected AbstractAsyncJobImpl(Session session, String nativeJobId, AbstractSyncJobServiceImpl service) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, nativeJobId, service);
    }
    
    //////////////////////////////////////////// interface Job ////////////////////////////////////////////

    public Task<Job, JobDescription> getJobDescription(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,JobDescription>().create(
                mode, m_session, this,
                "getJobDescriptionSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, OutputStream> getStdin(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,OutputStream>().create(
                mode, m_session, this,
                "getStdinSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, InputStream> getStdout(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,InputStream>().create(
                mode, m_session, this,
                "getStdoutSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, InputStream> getStderr(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,InputStream>().create(
                mode, m_session, this,
                "getStderrSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> suspend(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "suspendSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> resume(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "resumeSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> checkpoint(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "checkpointSync",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> migrate(TaskMode mode, JobDescription jd) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "migrateSync",
                new Class[]{JobDescription.class},
                new Object[]{jd});
    }

    public Task<Job, Void> signal(TaskMode mode, int signum) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "signalSync",
                new Class[]{int.class},
                new Object[]{signum});
    }
}
