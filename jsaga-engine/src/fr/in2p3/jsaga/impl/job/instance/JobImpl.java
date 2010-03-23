package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManager;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.TaskMode;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   5 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobImpl extends AbstractAsyncJobImpl implements Job {
    /** constructor for submission */
    public JobImpl(Session session, String nativeJobDesc, JobDescription jobDesc, DataStagingManager stagingMgr, String uniqId, AbstractSyncJobServiceImpl service) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, nativeJobDesc, jobDesc, stagingMgr, uniqId, service);
    }

    /** constructor for control and monitoring only */
    public JobImpl(Session session, String nativeJobId, DataStagingManager stagingMgr, AbstractSyncJobServiceImpl service) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, nativeJobId, stagingMgr, service);
    }

    ////////////////////////////////////////// interface Task ///////////////////////////////////////////

    public void run() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("run");
        if (timeout == WAIT_FOREVER) {
            super.run();
        } else {
            try {
                getResult(super.run(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public void cancel() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("cancel");
        if (timeout == WAIT_FOREVER) {
            super.cancel();
        } else {
            try {
                getResult(super.cancel(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public State getState() throws NotImplementedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getState");
        if (timeout == WAIT_FOREVER) {
            return super.getState();
        } else {
            try {
                return (State) getResult(super.getState(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    //////////////////////////////////////////// interface Job ////////////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getJobDescription");
        if (timeout == WAIT_FOREVER) {
            return super.getJobDescriptionSync();
        } else {
            try {
                return (JobDescription) getResult(super.getJobDescription(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
        }
    }

    public OutputStream getStdin() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // can not hang...
        return super.getStdinSync();
    }

    public InputStream getStdout() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // can not hang...
        return super.getStdoutSync();
    }

    public InputStream getStderr() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // can not hang...
        return super.getStderrSync();
    }

    public void suspend() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("suspend");
        if (timeout == WAIT_FOREVER) {
            super.suspendSync();
        } else {
            try {
                getResult(super.suspend(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public void resume() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("resume");
        if (timeout == WAIT_FOREVER) {
            super.resumeSync();
        } else {
            try {
                getResult(super.resume(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public void checkpoint() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("checkpoint");
        if (timeout == WAIT_FOREVER) {
            super.checkpointSync();
        } else {
            try {
                getResult(super.checkpoint(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public void migrate(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("migrate");
        if (timeout == WAIT_FOREVER) {
            super.migrateSync(jd);
        } else {
            try {
                getResult(super.migrate(TaskMode.ASYNC, jd), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public void signal(int signum) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("signal");
        if (timeout == WAIT_FOREVER) {
            super.signalSync(signum);
        } else {
            try {
                getResult(super.signal(TaskMode.ASYNC, signum), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////// private methods //////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(Job.class, methodName, m_resourceManager.getScheme());
    }
}
