package org.ogf.saga.job;

import java.io.InputStream;
import java.io.OutputStream;

import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

public interface Job extends Task, Async, Attributes, Permissions {

    /**
     * Retrieves the job description that was used to submit this job
     * instance.
     * @return the job description
     */
    public JobDescription getJobDescription()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, IncorrectState, NoSuccess;

    /**
     * Returns the input stream of this job (to which can be written).
     * @return the stream.
     */
    public OutputStream getStdin()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, DoesNotExist, Timeout,
            IncorrectState, NoSuccess;

    /**
     * Returns the output stream of this job (which can be read).
     * @return the stream.
     */
    public InputStream getStdout()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, DoesNotExist, Timeout,
            IncorrectState, NoSuccess;

    /**
     * Returns the error stream of this job (which can be read).
     * @return the stream.
     */
    public InputStream getStderr()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, DoesNotExist, Timeout,
            IncorrectState, NoSuccess;

    /**
     * Asks the resource manager to perform a suspend operation on a
     * running job.
     */
    public void suspend()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Asks the resource manager to perform a resume operation on a
     * suspended job.
     */
    public void resume()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Asks the resource manager to initiate a checkpoint operation on a
     * running job.
     */
    public void checkpoint()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Asks the resource manager to migrate a job.
     * @param jd new job parameters to apply when the job is migrated.
     */
    public void migrate(JobDescription jd)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Asks the resource manager to deliver an arbitrary signal to a
     * dispatched job.
     */
    public void signal(int signum)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    //
    // Task versions ...
    //

    /**
     * Creates a task that retrieves the job description that was used to
     * submit this job instance.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<JobDescription> getJobDescription(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the input stream of this job (to which can
     * be written).
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<OutputStream> getStdin(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the output stream of this job (which can
     * be read).
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<InputStream> getStdout(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the error stream of this job (which can
     * be read).
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<InputStream> getStderr(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that asks the resource manager to perform a suspend
     * operation on a running job.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task suspend(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that asks the resource manager to perform a resume
     * operation on a suspended job.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task resume(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that asks the resource manager to initiate a checkpoint
     * operation on a running job.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task checkpoint(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that asks the resource manager to migrate a job.
     * @param jd new job parameters to apply when the job is migrated.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task migrate(TaskMode mode, JobDescription jd)
        throws NotImplemented;

    /**
     * Creates a task that asks the resource manager to deliver an arbitrary
     * signal to a dispatched job.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task signal(TaskMode mode, int signum)
        throws NotImplemented;
}
