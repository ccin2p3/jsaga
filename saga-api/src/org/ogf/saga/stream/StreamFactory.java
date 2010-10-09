package org.ogf.saga.stream;

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

/**
 * Factory for objects from the stream package.
 */
public abstract class StreamFactory {

    private static StreamFactory factory;

    private static synchronized void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createStreamFactory();
        }
    }

    /**
     * Creates a Stream. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream service.
     * @return the stream.
     */
    protected abstract Stream doCreateStream(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException;

    /**
     * Creates a StreamService. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the service.
     * @return the service.
     */
    protected abstract StreamService doCreateStreamService(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException;

    /**
     * Creates a StreamService. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @return the service.
     */
    protected abstract StreamService doCreateStreamService(Session session)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException;

    /**
     * Creates a task that creates a Stream. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<StreamFactory, Stream> doCreateStream(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a StreamService. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<StreamFactory, StreamService> doCreateStreamService(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a StreamService. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<StreamFactory, StreamService> doCreateStreamService(
            TaskMode mode, Session session) throws NotImplementedException;

    /**
     * Creates a Stream.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the stream service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Stream createStream(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateStream(session, name);
    }

    /**
     * Creates a Stream using the default session.
     * 
     * @param name
     *      location of the stream service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Stream createStream(URL name) throws NotImplementedException,
            IncorrectURLException, BadParameterException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStream(session, name);
    }

    /**
     * Creates a StreamService.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the service.
     * @return
     *      the service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static StreamService createStreamService(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateStreamService(session, name);
    }

    /**
     * Creates a StreamService.
     * 
     * @param session
     *      the session handle.
     * @return
     *      the service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static StreamService createStreamService(Session session)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateStreamService(session);
    }

    /**
     * Creates a StreamService using the default session.
     * 
     * @param name
     *      location of the service.
     * @return
     *      the service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static StreamService createStreamService(URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(session, name);
    }

    /**
     * Creates a StreamService using the default session.
     * 
     * @return
     *      the service.
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
     * @exception BadParameterException
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static StreamService createStreamService()
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(session);
    }

    /**
     * Creates a task that creates a Stream.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<StreamFactory, Stream> createStream(TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateStream(mode, session, name);
    }

    /**
     * Creates a task that creates a Stream using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the stream service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<StreamFactory, Stream> createStream(TaskMode mode,
            URL name) throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStream(mode, session, name);
    }

    /**
     * Creates a task that creates a StreamService.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<StreamFactory, StreamService> createStreamService(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        initializeFactory();
        return factory.doCreateStreamService(mode, session, name);
    }

    /**
     * Creates a task that creates a StreamService.
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
    public static Task<StreamFactory, StreamService> createStreamService(
            TaskMode mode, Session session) throws NotImplementedException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateStreamService(mode, session);
    }

    /**
     * Creates a task that creates a StreamService using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the service.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<StreamFactory, StreamService> createStreamService(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(mode, session, name);
    }

    /**
     * Creates a task that creates a StreamService using the default session.
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
    public static Task<StreamFactory, StreamService> createStreamService(
            TaskMode mode) throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(mode, session);
    }
}
