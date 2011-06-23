package org.ogf.saga.job;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/**
 * Factory for objects from the job package.
 */
public abstract class JobFactory {
    
    private static JobFactory getFactory(String sagaFactoryName)
            throws NoSuccessException, NotImplementedException {
	return ImplementationBootstrapLoader.getJobFactory(sagaFactoryName);
    }

    /**
     * Creates a job description. To be provided by the implementation.
     * 
     * @return the job description.
     */
    protected abstract JobDescription doCreateJobDescription()
            throws NotImplementedException, NoSuccessException;

    /**
     * Creates a job service. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param rm
     *            contact string for the resource manager.
     * @return the job service.
     */
    protected abstract JobService doCreateJobService(Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Creates a task that creates a job service. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param rm
     *            contact string for the resource manager.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<JobFactory, JobService> doCreateJobService(
            TaskMode mode, Session session, URL rm)
            throws NotImplementedException;

    /**
     * Creates a job description.
     * 
     * @return
     *      the job description.
     * @exception NotImplementedException
     *      is thrown when this method is not implemented.
     * @throws NoSuccessException
     *      is thrown when the Saga factory could not be created.
     */
    public static JobDescription createJobDescription()
            throws NotImplementedException, NoSuccessException {
        return createJobDescription(null);
    }
    
    /**
     * Creates a job description.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the job description.
     * @exception NotImplementedException
     *      is thrown when this method is not implemented.
     * @throws NoSuccessException
     *      is thrown when the Saga factory could not be created.
     */
    public static JobDescription createJobDescription(String sagaFactoryClassname)
            throws NotImplementedException, NoSuccessException {
        return getFactory(sagaFactoryClassname).doCreateJobDescription();
    }

    /**
     * Creates a job service.
     * 
     * @param session
     *      the session handle.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if the specified URL cannot be contacted, or a
     *      default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createJobService((String)null, session, rm);
    }
    

    /**
     * Creates a job service.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if the specified URL cannot be contacted, or a
     *      default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(String sagaFactoryClassname, Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
	if (rm == null) {
	    rm = URLFactory.createURL(sagaFactoryClassname, "");
	}
        return getFactory(sagaFactoryClassname).doCreateJobService(session, rm);
    }


    /**
     * Creates a job service using the default contact string.
     * 
     * @param session
     *      the session handle.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if a default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(Session session)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createJobService(session, (URL) null);
    }
    

    /**
     * Creates a job service using the default contact string.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if a default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(String sagaFactoryClassname, Session session)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createJobService(sagaFactoryClassname, session, (URL) null);
    }


    /**
     * Creates a job service, using the default session.
     * 
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if the specified URL cannot be contacted, or a
     *      default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createJobService((Session) null, rm);
    }
    /**
     * Creates a job service, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if the specified URL cannot be contacted, or a
     *      default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(String sagaFactoryClassname, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createJobService(sagaFactoryClassname, (Session) null, rm);
    }
    

    /**
     * Creates a job service, using the default session and default contact
     * string.
     * 
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if a default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService() throws NotImplementedException,
            BadParameterException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createJobService((Session) null);
    }
    

    /**
     * Creates a job service, using the default session and default contact
     * string.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the job service.
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
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception BadParameterException
     *      is thrown if a default contact point does not exist or cannot be found.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static JobService createJobService(String sagaFactoryClassname) throws NotImplementedException,
            BadParameterException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createJobService(sagaFactoryClassname, (Session) null);
    }


    /**
     * Creates a task that creates a job service.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param rm
     *            contact string for the resource manager.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(TaskMode mode,
            Session session, URL rm) throws NotImplementedException,
            NoSuccessException {
        return createJobService((String) null, mode, session, rm);
    }
    
    /**
     * Creates a task that creates a job service.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param rm
     *            contact string for the resource manager.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(String sagaFactoryClassname, TaskMode mode,
            Session session, URL rm) throws NotImplementedException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
	if (rm == null) {
	    try {
		rm = URLFactory.createURL(sagaFactoryClassname, "");
	    } catch (BadParameterException e) {
		// Should not happen
		throw new NoSuccessException("Unexpected exception", e);
	    }
	}
        return getFactory(sagaFactoryClassname).doCreateJobService(mode, session, rm);
    }

    /**
     * Creates a task that creates a job service, using a default contact
     * string.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(TaskMode mode,
            Session session) throws NotImplementedException, NoSuccessException {
        return createJobService(mode, session, (URL) null);
    }
    

    /**
     * Creates a task that creates a job service, using a default contact
     * string.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(String sagaFactoryClassname, TaskMode mode,
            Session session) throws NotImplementedException, NoSuccessException {
        return createJobService(sagaFactoryClassname, mode, session, (URL) null);
    }

    /**
     * Creates a task that creates a job service, using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param rm
     *            contact string for the resource manager.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(TaskMode mode,
            URL rm) throws NotImplementedException, NoSuccessException {
        return createJobService(mode, (Session) null, rm);
    }
    
    /**
     * Creates a task that creates a job service, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param rm
     *            contact string for the resource manager.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(String sagaFactoryClassname, TaskMode mode,
            URL rm) throws NotImplementedException, NoSuccessException {
        return createJobService(sagaFactoryClassname, mode, (Session) null, rm);
    }

    /**
     * Creates a task that creates a job service, using the default session and
     * default contact string.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(TaskMode mode)
            throws NotImplementedException, NoSuccessException {
        return createJobService(mode, (URL) null);
    }
    

    /**
     * Creates a task that creates a job service, using the default session and
     * default contact string.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<JobFactory, JobService> createJobService(String sagaFactoryClassname, TaskMode mode)
            throws NotImplementedException, NoSuccessException {
        return createJobService(sagaFactoryClassname, mode, (URL) null);
    }
}
