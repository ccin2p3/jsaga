package org.ogf.saga.job;

import java.util.List;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * A JobService represents a resource management back-end.
 * 
 * It allows for job creation, submission, and discovery. Deviation from the
 * SAGA specification: the convenience method <code>runJob</code> is specified
 * differently here, because as described in the SAGA specifications it cannot
 * easily be specified in Java, since Java has no OUT parameters.
 */
public interface JobService extends SagaObject, Async {

    /**
     * Creates a job instance as specified by the job description provided. The
     * job is delivered in 'New' state. The provided job description is copied,
     * so can be modified after this call.
     * 
     * @param jd
     *      the job description.
     * @return
     *      the job.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the job description does not contain a valid
     *      EXECUTABLE, or contains invalid values.
     */
    public Job createJob(JobDescription jd) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Runs the specified command on the specified host. Deviation from the SAGA
     * specification: the input, output and error stream OUT parameters are not
     * specified here, since Java has no OUT parameters. Unfortunately, their
     * absence, according to the SAGA specifications, implies a non-interactive
     * job. Since interactive jobs should still be supported, a parameter is
     * added here to specify whether the job is interactive. If interactive, the
     * streams can be obtained from the Job using the {@link Job#getStdin()},
     * {@link Job#getStdout()}, and {@link Job#getStderr()} methods.
     * 
     * @param commandLine
     *      the command to run.
     * @param host
     *      hostname of the host on which the command must be run. If this
     *      is an empty string, the implementation is free to choose a
     *      host.
     * @param interactive
     *      specifies whether the job is interactive.
     * @return
     *      the job.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the command line cannot be parsed.
     */
    public Job runJob(String commandLine, String host, boolean interactive)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Runs the specified command, non-interactively, on the specified host.
     * Deviation from the SAGA specification: the input, output and error stream
     * OUT parameters are not specified here, since Java has no OUT parameters.
     * 
     * @param commandLine
     *      the command to run.
     * @param host
     *      hostname of the host on which the command must be run. If this
     *      is an empty string, the implementation is free to choose a
     *      host.
     * @return
     *      the job.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the command line cannot be parsed.
     */
    public Job runJob(String commandLine, String host)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Runs the specified command on a host chosen by the implementation.
     * Deviation from the SAGA specification: the input, output and error stream
     * OUT parameters are not specified here, since Java has no OUT parameters.
     * Unfortunately, their absence, according to the SAGA specifications,
     * implies a non-interactive job. Since interactive jobs should still be
     * supported, a parameter is added here to specify whether the job is
     * interactive. If interactive, the streams can be obtained from the Job
     * using the {@link Job#getStdin()}, {@link Job#getStdout()}, and
     * {@link Job#getStderr()} methods.
     * 
     * @param commandLine
     *      the command to run.
     * @param interactive
     *      specifies whether the job is interactive.
     * @return
     *      the job.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the command line cannot be parsed.
     */
    public Job runJob(String commandLine, boolean interactive)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Runs the specified command, non-interactively, on a host chosen by the
     * implementation. Deviation from the SAGA specification: the input, output
     * and error stream OUT parameters are not specified here, since Java has no
     * OUT parameters.
     * 
     * @param commandLine
     *            the command to run.
     * @return the job.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the command line cannot be parsed.
     */
    public Job runJob(String commandLine) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Obtains the list of jobs that are currently known to the resource
     * manager.
     * 
     * @return
     *      a list of job identifications.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public List<String> list() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Returns the job instance associated with the specified job
     * identification.
     * 
     * @param jobId
     *      the job identification.
     * @return
     *      the job instance.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the JobService cannot parse the job id.
     * @exception DoesNotExistException
     *      is thrown when the JobService can handle the job id, but the
     *      referenced job is not alive.
     */
    public Job getJob(String jobId) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Returns a job instance representing the calling application.
     * 
     * @return
     *      the job instance.
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
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public JobSelf getSelf() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Creates a task that creates a job instance as specified by the job
     * description provided. The job is delivered in 'New' state.
     * 
     * @param mode
     *            the task mode.
     * @param jd
     *            the job description.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<JobService, Job> createJob(TaskMode mode, JobDescription jd)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the list of jobs that are currently known to
     * the resource manager.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<JobService, List<String>> list(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the job instance associated with the
     * specified job identification.
     * 
     * @param mode
     *            the task mode.
     * @param jobId
     *            the job identification.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<JobService, Job> getJob(TaskMode mode, String jobId)
            throws NotImplementedException;

    /**
     * Creates a task that obtains a job instance representing the calling
     * application.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<JobService, JobSelf> getSelf(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that runs the specified command on the specified host.
     * Deviation from the SAGA specification: the input, output and error stream
     * OUT parameters are not specified here, since Java has no OUT parameters.
     * Unfortunately, their absence, according to the SAGA specifications,
     * implies a non-interactive job. Since interactive jobs should still be
     * supported, a parameter is added here to specify whether the job is
     * interactive. If interactive, the streams can be obtained from the Job
     * using the {@link Job#getStdin()}, {@link Job#getStdout()}, and
     * {@link Job#getStderr()} methods
     * 
     * @param mode
     *            the task mode.
     * @param commandLine
     *            the command to run.
     * @param host
     *            hostname of the host on which the command must be run. If this
     *            is an empty string, the implementation is free to choose a
     *            host.
     * @param interactive
     *            specifies whether the job is interactive.
     * @return the task.
     */
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine,
            String host, boolean interactive) throws NotImplementedException;

    /**
     * Creates a task that runs the specified command, non-interactively, on the
     * specified host. Deviation from the SAGA specification: the input, output
     * and error stream OUT parameters are not specified here, since Java has no
     * OUT parameters.
     * 
     * @param mode
     *            the task mode.
     * @param commandLine
     *            the command to run.
     * @param host
     *            hostname of the host on which the command must be run. If this
     *            is an empty string, the implementation is free to choose a
     *            host.
     * @return the task.
     */
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine,
            String host) throws NotImplementedException;

    /**
     * Creates a task that runs the specified command on a host chosen by the
     * implementation. Deviation from the SAGA specification: the input, output
     * and error stream OUT parameters are not specified here, since Java has no
     * OUT parameters. Unfortunately, their absence, according to the SAGA
     * specifications, implies a non-interactive job. Since interactive jobs
     * should still be supported, a parameter is added here to specify whether
     * the job is interactive. If interactive, the streams can be obtained from
     * the Job using the {@link Job#getStdin()}, {@link Job#getStdout()}, and
     * {@link Job#getStderr()} methods.
     * 
     * @param mode
     *            the task mode.
     * @param commandLine
     *            the command to run.
     * @param interactive
     *            specifies whether the job is interactive.
     * @return the task.
     */
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine,
            boolean interactive) throws NotImplementedException;

    /**
     * Creates a task that runs the specified command, non-interactively, on a
     * host chosen by the implementation. Deviation from the SAGA specification:
     * the input, output and error stream OUT parameters are not specified here,
     * since Java has no OUT parameters.
     * 
     * @param mode
     *            the task mode.
     * @param commandLine
     *            the command to run.
     * @return the task.
     */
    public Task<JobService, Job> runJob(TaskMode mode, String commandLine)
            throws NotImplementedException;
}
