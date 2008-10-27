package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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

    public Job runJob(String commandLine, String host, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
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
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }
    public Job runJob(String commandLine, String host) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, host, false);
    }
    public Job runJob(String commandLine, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, "", interactive);
    }
    public Job runJob(String commandLine) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, "", false);
    }

    public Task<JobService, Job> createJob(TaskMode mode, JobDescription jd) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "createJob",
                new Class[]{JobDescription.class},
                new Object[]{jd});
    }

    public Task<JobService, List<String>> list(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,List<String>>().create(
                mode, m_session, this,
                "list",
                new Class[]{},
                new Object[]{});
    }

    public Task<JobService, Job> getJob(TaskMode mode, String jobId) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "getJob",
                new Class[]{String.class},
                new Object[]{jobId});
    }

    public Task<JobService, JobSelf> getSelf(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,JobSelf>().create(
                mode, m_session, this,
                "getSelf",
                new Class[]{},
                new Object[]{});
    }

    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, String host, boolean interactive) throws NotImplementedException {
        return new GenericThreadedTaskFactory<JobService,Job>().create(
                mode, m_session, this,
                "runJob",
                new Class[]{String.class, String.class, boolean.class},
                new Object[]{commandLine, host, interactive});
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, String host) throws NotImplementedException {
        return this.runJob(mode, commandLine, host, false);
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine, boolean interactive) throws NotImplementedException {
        return this.runJob(mode, commandLine, "", interactive);
    }
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine) throws NotImplementedException {
        return this.runJob(mode, commandLine, "", false);
    }
}
