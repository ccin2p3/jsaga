package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.permissions.AbstractJobPermissionsImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;

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
public abstract class AbstractAsyncJobImpl extends AbstractJobPermissionsImpl implements Job {
    /** constructor */
    public AbstractAsyncJobImpl(Session session, boolean create) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, create);
    }

    //////////////////////////////////////////// interface Task ////////////////////////////////////////////

    public Task<Job, Void> run(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "run",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, State> getState(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,State>().create(
                mode, m_session, this,
                "getState",
                new Class[]{},
                new Object[]{});
    }

    //////////////////////////////////////////// interface Job ////////////////////////////////////////////

    public Task<Job, JobDescription> getJobDescription(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,JobDescription>().create(
                mode, m_session, this,
                "getJobDescription",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, OutputStream> getStdin(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,OutputStream>().create(
                mode, m_session, this,
                "getStdin",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, InputStream> getStdout(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,InputStream>().create(
                mode, m_session, this,
                "getStdout",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, InputStream> getStderr(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,InputStream>().create(
                mode, m_session, this,
                "getStderr",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> suspend(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "suspend",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> resume(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "resume",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> checkpoint(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "checkpoint",
                new Class[]{},
                new Object[]{});
    }

    public Task<Job, Void> migrate(TaskMode mode, JobDescription jd) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "migrate",
                new Class[]{JobDescription.class},
                new Object[]{jd});
    }

    public Task<Job, Void> signal(TaskMode mode, int signum) throws NotImplementedException {
        return new GenericThreadedTaskFactory<Job,Void>().create(
                mode, m_session, this,
                "signal",
                new Class[]{int.class},
                new Object[]{signum});
    }
}
