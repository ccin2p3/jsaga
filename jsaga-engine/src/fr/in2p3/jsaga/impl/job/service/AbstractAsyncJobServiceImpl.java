package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;
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
public abstract class AbstractAsyncJobServiceImpl extends AbstractSagaObjectImpl implements JobService {
    /** constructor */
    public AbstractAsyncJobServiceImpl(Session session) {
        super(session);
    }

    public Job runJob(String commandLine, String host, boolean interactive) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            // set job description
            JobDescription desc = JobFactory.createJobDescription();
            desc.setAttribute(JobDescription.EXECUTABLE, commandLine);
            desc.setAttribute(JobDescription.INTERACTIVE, ""+interactive);
            desc.setAttribute(JobDescription.CANDIDATEHOSTS, ""+host);

            // set job service
            Session session = SessionFactory.createSession(true);
            URL serviceURL = URLFactory.createURL(host);
            JobService service = JobFactory.createJobService(session, serviceURL);

            // submit job
            Job job = service.createJob(desc);
            job.run();
            return job;
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }
    public Job runJob(String commandLine, String host) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, host, false);
    }
    public Job runJob(String commandLine, boolean interactive) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, "", interactive);
    }
    public Job runJob(String commandLine) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, "", false);
    }

    public Task<Job> createJob(TaskMode mode, JobDescription jd) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobServiceImpl.class.getMethod("createJob", new Class[]{JobDescription.class}),
                    new Object[]{jd}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<List<String>> list(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobServiceImpl.class.getMethod("list", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Job> getJob(TaskMode mode, String jobId) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobServiceImpl.class.getMethod("getJob", new Class[]{String.class}),
                    new Object[]{jobId}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<JobSelf> getSelf(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobServiceImpl.class.getMethod("getSelf", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Job> runJob(TaskMode mode, String commandLine, String host, boolean interactive) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobServiceImpl.class.getMethod("runJob", new Class[]{String.class, String.class, boolean.class}),
                    new Object[]{commandLine, host, interactive}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<Job> runJob(TaskMode mode, String commandLine, String host) throws NotImplemented {
        return this.runJob(mode, commandLine, host, false);
    }
    public Task<Job> runJob(TaskMode mode, String commandLine, boolean interactive) throws NotImplemented {
        return this.runJob(mode, commandLine, "", interactive);
    }
    public Task<Job> runJob(TaskMode mode, String commandLine) throws NotImplemented {
        return this.runJob(mode, commandLine, "", false);
    }
}
