package org.ogf.saga.job;

import org.ogf.saga.URI;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectSession;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * Factory for objects from the job package.
 */
public abstract class JobFactory {
    
    private static JobFactory factory;

    private static synchronized void initializeFactory() 
        throws NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createJobFactory();
        }
    }
    
    /**
     * Creates a job description. To be provided by the implementation.
     * @return the job description.
     */
    protected abstract JobDescription doCreateJobDescription()
        throws NotImplemented;
    
    /**
     * Creates a job service. To be provided by the implementation.
     * @param session the session handle.
     * @param rm contact string for the resource manager.
     * @return the job service.
     */
    protected abstract JobService doCreateJobService(
            Session session, URI rm) 
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Creates a task that creates a job service.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param rm contact string for the resource manager.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract RVTask<JobService> doCreateJobService(
            TaskMode mode, Session session, URI rm)
        throws NotImplemented;
    
    /**
     * Creates a job description.
     * @return the job description.
     */
    public static JobDescription createJobDescription()
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateJobDescription();
    }
    
    /**
     * Creates a job service.
     * @param session the session handle.
     * @param rm contact string for the resource manager.
     * @return the job service.
     */
    public static JobService createJobService(
            Session session, URI rm) 
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            DoesNotExist, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateJobService(session, rm);
    }

    /**
     * Creates a task that creates a job service.
     * @param mode the task mode.
     * @param session the session handle.
     * @param rm contact string for the resource manager.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static RVTask<JobService> createJobService(
            TaskMode mode, Session session, URI rm) throws NotImplemented {
        initializeFactory();
        return factory.doCreateJobService(mode, session, rm);
    }
}
