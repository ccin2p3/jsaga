package org.ogf.saga.job;

import java.io.InputStream;
import java.io.OutputStream;

import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * Jobs are created by a {@link JobService}, using a {@link JobDescription}.
 * The <code>Job</code> interface extends the
 * {@link org.ogf.saga.task.Task Task} interface, but jobs don't have an
 * associated Saga object or a result object, so the generic instantiation
 * parameters are {@link java.lang.Void Void}.
 */
public interface Job extends Task<Void, Void>, Async, AsyncAttributes<Job>,
        Permissions<Job> {

    // Required attributes:

    /** Attribute name, SAGA representation of the job identifier. */
    public static final String JOBID = "JobID";
    
    /** Attribute name, URL representation of the JobService that created the job. */
    public static final String SERVICEURL = "ServiceURL";

    // Optional attributes:

    /**
     * Attribute name, list of host names or IP addresses allocated to run this
     * job.
     */
    public static final String EXECUTIONHOSTS = "ExecutionHosts";

    /**
     * Attribute name, time stamp of the job creation in the resource manager.
     */
    public static final String CREATED = "Created";

    /** Attribute name, time stamp indicating when the job started running. */
    public static final String STARTED = "Started";

    /** Attribute name, time stamp indicating when the job finished. */
    public static final String FINISHED = "Finished";

    /** Attribute name, working directory on the execution host. */
    public static final String WORKINGDIRECTORY = "WorkingDirectory";

    /** Attribute name, process exit code. */
    public static final String EXITCODE = "ExitCode";

    /** Attribute name, signal number which caused the job to exit. */
    public static final String TERMSIG = "Termsig";

    // Required metrics:

    /** Metric name, fires on state changes of the job. */
    public static final String JOB_STATE = "job.state";

    // Optional metrics:

    /** Metric name, fires as a job changes its state detail. */
    public static final String JOB_STATEDETAIL = "job.state_detail";

    /** Metric name, fires as a job receives a signal. */
    public static final String JOB_SIGNAL = "job.signal";

    /** Metric name, number of CPU seconds consumed by the job. */
    public static final String JOB_CPUTIME = "job.cpu_time";

    /** Metric name, current aggregate memory usage of the job. */
    public static final String JOB_MEMORYUSE = "job.memory_use";

    /** Metric name, current aggregate virtual memory usage of the job. */
    public static final String JOB_VMEMORYUSE = "job.vmemory_use";

    /** Metric name, current performance. */
    public static final String JOB_PERFORMANCE = "job.performance";

    // Methods

    /**
     * Retrieves the job description that was used to submit this job instance.
     * 
     * @return
     *      the job description
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown in cases where the job description is not available,
     *      for instance when the job was not submitted through SAGA and the
     *      job was obtained using the {@link JobService#getJob(String)} call.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public JobDescription getJobDescription() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Returns the input stream of this job (to which can be written).
     * 
     * @return
     *      the stream.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown when the stream is not available for some reason.
     * @exception IncorrectStateException
     *      is thrown when the job is not interactive.
     * @exception BadParameterException
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public OutputStream getStdin() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Returns the output stream of this job (which can be read).
     * 
     * @return
     *      the stream.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown when the stream is not available for some reason.
     * @exception IncorrectStateException
     *      is thrown when the job is not interactive.
     * @exception BadParameterException
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public InputStream getStdout() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Returns the error stream of this job (which can be read).
     * 
     * @return
     *      the stream.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown when the stream is not available for some reason.
     * @exception IncorrectStateException
     *      is thrown when the job is not interactive.
     * @exception BadParameterException
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public InputStream getStderr() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, IncorrectStateException,
            NoSuccessException;

    /**
     * Asks the resource manager to perform a suspend operation on a running
     * job.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the job is not in RUNNING state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void suspend() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to perform a resume operation on a suspended
     * job.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the job is not in SUSPENDED state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void resume() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to initiate a checkpoint operation on a running
     * job.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the job is not in RUNNING state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void checkpoint() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to migrate a job.
     * 
     * @param jd
     *      new job parameters to apply when the job is migrated.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the job is not in RUNNING or SUSPENDED state.
     * @exception BadParameterException
     *      is thrown if there is something wrong with the specified
     *      job description.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void migrate(JobDescription jd) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Asks the resource manager to deliver an arbitrary signal to a dispatched
     * job.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the job is not in RUNNING or SUSPENDED state.
     * @exception BadParameterException
     *      is thrown if the specified signal is not supported by the backend.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void signal(int signum) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Creates a task that retrieves the job description that was used to submit
     * this job instance.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, JobDescription> getJobDescription(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the input stream of this job (to which can be
     * written).
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, OutputStream> getStdin(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the output stream of this job (which can be
     * read).
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, InputStream> getStdout(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the error stream of this job (which can be
     * read).
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, InputStream> getStderr(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that asks the resource manager to perform a suspend
     * operation on a running job.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, Void> suspend(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that asks the resource manager to perform a resume
     * operation on a suspended job.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, Void> resume(TaskMode mode) throws NotImplementedException;

    /**
     * Creates a task that asks the resource manager to initiate a checkpoint
     * operation on a running job.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, Void> checkpoint(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that asks the resource manager to migrate a job.
     * 
     * @param jd
     *            new job parameters to apply when the job is migrated.
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, Void> migrate(TaskMode mode, JobDescription jd)
            throws NotImplementedException;

    /**
     * Creates a task that asks the resource manager to deliver an arbitrary
     * signal to a dispatched job.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Job, Void> signal(TaskMode mode, int signum)
            throws NotImplementedException;
}
