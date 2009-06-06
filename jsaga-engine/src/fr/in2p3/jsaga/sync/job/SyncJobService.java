package fr.in2p3.jsaga.sync.job;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.task.Async;

import java.util.List;

/**
 * A JobService represents a resource management back-end.
 *
 * It allows for job creation, submission, and discovery. Deviation from the
 * SAGA specification: the convenience method <code>runJob</code> is specified
 * differently here, because as described in the SAGA specifications it cannot
 * easily be specified in Java, since Java has no OUT parameters.
 */
public interface SyncJobService extends SagaObject, Async {

    /**
     * Creates a job instance as specified by the job description provided. The
     * job is delivered in 'New' state. The provided job description is copied,
     * so can be modified after this call.
     *
     * @param jd
     *            the job description.
     * @return the job.
     */
    public Job createJobSync(JobDescription jd) throws NotImplementedException,
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
     *            the command to run.
     * @param host
     *            hostname of the host on which the command must be run. If this
     *            is an empty string, the implementation is free to choose a
     *            host.
     * @param interactive
     *            specifies whether the job is interactive.
     * @return the job.
     */
    public Job runJobSync(String commandLine, String host, boolean interactive)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Runs the specified command, non-interactively, on the specified host.
     * Deviation from the SAGA specification: the input, output and error stream
     * OUT parameters are not specified here, since Java has no OUT parameters.
     *
     * @param commandLine
     *            the command to run.
     * @param host
     *            hostname of the host on which the command must be run. If this
     *            is an empty string, the implementation is free to choose a
     *            host.
     * @return the job.
     */
    public Job runJobSync(String commandLine, String host)
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
     *            the command to run.
     * @param interactive
     *            specifies whether the job is interactive.
     * @return the job.
     */
    public Job runJobSync(String commandLine, boolean interactive)
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
     */
    public Job runJobSync(String commandLine) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Obtains the list of jobs that are currently known to the resource
     * manager.
     *
     * @return a list of job identifications.
     */
    public List<String> listSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Returns the job instance associated with the specified job
     * identification.
     *
     * @param jobId
     *            the job identification.
     * @return the job instance.
     */
    public Job getJobSync(String jobId) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Returns a job instance representing the calling application.
     *
     * @return the job instance.
     */
    public JobSelf getSelfSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;
}
