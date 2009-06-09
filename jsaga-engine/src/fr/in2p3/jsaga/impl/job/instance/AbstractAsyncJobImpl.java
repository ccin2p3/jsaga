package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.DataStagingDescription;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
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
        return new AbstractThreadedTask<Job,JobDescription>(mode) {
            public JobDescription invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobImpl.super.getJobDescriptionSync();
            }
        };
    }

    public Task<Job, OutputStream> getStdin(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,OutputStream>(mode) {
            public OutputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobImpl.super.getStdinSync();
            }
        };
    }

    public Task<Job, InputStream> getStdout(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,InputStream>(mode) {
            public InputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobImpl.super.getStdoutSync();
            }
        };
    }

    public Task<Job, InputStream> getStderr(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,InputStream>(mode) {
            public InputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncJobImpl.super.getStderrSync();
            }
        };
    }

    public Task<Job, Void> suspend(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncJobImpl.super.suspendSync();
                return null;
            }
        };
    }

    public Task<Job, Void> resume(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncJobImpl.super.resumeSync();
                return null;
            }
        };
    }

    public Task<Job, Void> checkpoint(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Job,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncJobImpl.super.checkpointSync();
                return null;
            }
        };
    }

    public Task<Job, Void> migrate(TaskMode mode, final JobDescription jd) throws NotImplementedException {
        return new AbstractThreadedTask<Job,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncJobImpl.super.migrateSync(jd);
                return null;
            }
        };
    }

    public Task<Job, Void> signal(TaskMode mode, final int signum) throws NotImplementedException {
        return new AbstractThreadedTask<Job,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncJobImpl.super.signalSync(signum);
                return null;
            }
        };
    }
}
