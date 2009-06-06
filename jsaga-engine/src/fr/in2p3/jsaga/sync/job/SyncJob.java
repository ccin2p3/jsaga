package fr.in2p3.jsaga.sync.job;

import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jobs are created by a {@link SyncJobService}, using a {@link JobDescription}.
 * The <code>Job</code> interface extends the
 * {@link org.ogf.saga.task.Task Task} interface, but jobs don't have an
 * associated Saga object or a result object, so the generic instantiation
 * parameters are {@link java.lang.Void Void}.
 */
public interface SyncJob {

    /**
     * Retrieves the job description that was used to submit this job instance.
     *
     * @return the job description
     */
    public JobDescription getJobDescriptionSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Returns the input stream of this job (to which can be written).
     *
     * @return the stream.
     */
    public OutputStream getStdinSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Returns the output stream of this job (which can be read).
     *
     * @return the stream.
     */
    public InputStream getStdoutSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Returns the error stream of this job (which can be read).
     *
     * @return the stream.
     */
    public InputStream getStderrSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Asks the resource manager to perform a suspend operation on a running
     * job.
     */
    public void suspendSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to perform a resume operation on a suspended
     * job.
     */
    public void resumeSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to initiate a checkpoint operation on a running
     * job.
     */
    public void checkpointSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to migrate a job.
     *
     * @param jd
     *            new job parameters to apply when the job is migrated.
     */
    public void migrateSync(JobDescription jd) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to deliver an arbitrary signal to a dispatched
     * job.
     */
    public void signalSync(int signum) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;
}
