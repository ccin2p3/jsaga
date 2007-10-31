package org.ogf.saga.job;

import java.util.List;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * A JobService represents a resource management back-end.
 * It allows for job creation, submission, and discovery.
 * Deviation from the SAGA specification: the convenience method
 * <code>runJob</code> is not included here, because it cannot
 * easily be specified in Java, since Java has no OUT parameters. 
 */
public interface JobService extends SagaObject, Async {

    /**
     * Creates a job instance as specified by the job description provided.
     * The job is delivered in 'New' state. The provided job description
     * is copied, so can be modified after this call.
     * @param jd the job description.
     * @return the job.
     */
    public Job createJob(JobDescription jd) throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           BadParameter, Timeout, NoSuccess;

    /**
     * Obtains the list of jobs that are currently known to the
     * resource manager.
     * @return a list of job identifications.
     */
    public List<String> list() throws NotImplemented, AuthenticationFailed,
           AuthorizationFailed, PermissionDenied, Timeout, NoSuccess;

    /**
     * Returns the job instance associated with the specified job
     * identification.
     * @param jobId the job identification.
     * @return the job instance.
     */
    public Job getJob(String jobId) throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           BadParameter, DoesNotExist, Timeout, NoSuccess;

    /**
     * Returns a job instance representing the calling application.
     * @return the job instance.
     */
    public JobSelf getSelf() throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           Timeout, NoSuccess;

    /**
     * Creates a task that creates a job instance as specified by the
     * job description provided.
     * The job is delivered in 'New' state.
     * @param mode the task mode.
     * @param jd the job description.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Job> createJob(TaskMode mode, JobDescription jd)
        throws NotImplemented;

    /**
     * Creates a task that obtains the list of jobs that are currently known
     * to the resource manager.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<String>> list(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the job instance associated with the
     * specified job identification.
     * @param mode the task mode.
     * @param jobId the job identification.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Job> getJob(TaskMode mode, String jobId)
        throws NotImplemented;

    /**
     * Creates a task that obtains a job instance representing the calling
     * application.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<JobSelf> getSelf(TaskMode mode)
        throws NotImplemented;
}
