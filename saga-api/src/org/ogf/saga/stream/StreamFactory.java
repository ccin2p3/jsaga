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
import org.ogf.saga.url.URLFactory;

/**
 * Factory for objects from the stream package.
 */
public abstract class StreamFactory {
    
    private static final String DEFAULT_SERVER_URL = "";
    
    private static StreamFactory getFactory(String sagaFactoryName)
    throws NoSuccessException, NotImplementedException {
	return ImplementationBootstrapLoader.getStreamFactory(sagaFactoryName);
    }
    
    private static URL createDefaultURL() {
        URL url;
        try {
            url = URLFactory.createURL(DEFAULT_SERVER_URL);
        } catch (Throwable e) {
            // Cannot happen.
            throw new Error("Implementation error", e);
                        
        }
        return url;
    }

    /**
     * Creates a Stream. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream server.
     * @return the stream.
     */
    protected abstract Stream doCreateStream(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException;

    /**
     * Creates a StreamServer. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the server.
     * @return the stream server.
     */
    protected abstract StreamServer doCreateStreamServer(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
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
     *            location of the stream server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<StreamFactory, Stream> doCreateStream(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a StreamServer. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<StreamFactory, StreamServer> doCreateStreamServer(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException;

    /**
     * Creates a Stream.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the stream server.
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
        return createStream((String) null, session, name);
    }
    
    /**
     * Creates a Stream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the stream server.
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
    public static Stream createStream(String sagaFactoryClassname, Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateStream(session, name);
    }

    /**
     * Creates a Stream using the default session.
     * 
     * @param name
     *      location of the stream server.
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
        return createStream((Session) null, name);
    }
    

    /**
     * Creates a Stream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the stream server.
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
    public static Stream createStream(String sagaFactoryClassname, URL name) throws NotImplementedException,
            IncorrectURLException, BadParameterException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException {
        return createStream(sagaFactoryClassname, (Session) null, name);
    }


    /**
     * Creates a StreamServer.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the server.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer((String) null, session, name);
    }
    

    /**
     * Creates a StreamServer.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the server.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(String sagaFactoryClassname, Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateStreamServer(session, name);
    }

    /**
     * Creates a StreamServer.
     *
     * @param session
     *      the session handle.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(Session session)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer(session, createDefaultURL());
    }
    
    /**
     * Creates a StreamServer.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(String sagaFactoryClassname, Session session)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer(sagaFactoryClassname, session, createDefaultURL());
    }


    /**
     * Creates a StreamServer using the default session.
     * 
     * @param name
     *      location of the stream server.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer((Session) null, name);
    }
    

    /**
     * Creates a StreamServer using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the stream server.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer(sagaFactoryClassname, (Session) null, name);
    }


    /**
     * Creates a StreamServer using the default session.
     * 
     * @return
     *      the server.
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
    public static StreamServer createStreamServer()
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer((Session) null);
    }
    

    /**
     * Creates a StreamServer using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the server.
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
    public static StreamServer createStreamServer(String sagaFactoryClassname)
            throws NotImplementedException, IncorrectURLException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException {
        return createStreamServer(sagaFactoryClassname, (Session) null);
    }


    /**
     * Creates a task that creates a Stream.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream server.
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
        return createStream(null, mode, session, name);
    }
    

    /**
     * Creates a task that creates a Stream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the stream server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<StreamFactory, Stream> createStream(String sagaFactoryClassname, TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateStream(mode, session, name);
    }

    /**
     * Creates a task that creates a Stream using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the stream server.
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
        return createStream(mode, null, name);
    }
    
    /**
     * Creates a task that creates a Stream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the stream server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<StreamFactory, Stream> createStream(String sagaFactoryClassname, TaskMode mode,
            URL name) throws NotImplementedException, NoSuccessException {
        return createStream(sagaFactoryClassname, mode, null, name);
    }

    /**
     * Creates a task that creates a StreamServer.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<StreamFactory, StreamServer> createStreamServer(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createStreamServer(null, mode, session, name);
    }
    
    /**
     * Creates a task that creates a StreamServer.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<StreamFactory, StreamServer> createStreamServer(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateStreamServer(mode, session, name);
    }


    /**
     * Creates a task that creates a StreamServer.
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
    public static Task<StreamFactory, StreamServer> createStreamServer(
            TaskMode mode, Session session) throws NotImplementedException,
            NoSuccessException {
        return createStreamServer(mode, session, createDefaultURL());
    }
    

    /**
     * Creates a task that creates a StreamServer.
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
    public static Task<StreamFactory, StreamServer> createStreamServer(
            String sagaFactoryClassname, TaskMode mode, Session session) throws NotImplementedException,
            NoSuccessException {
        return createStreamServer(sagaFactoryClassname, mode, session, createDefaultURL());
    }

    /**
     * Creates a task that creates a StreamServer using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<StreamFactory, StreamServer> createStreamServer(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createStreamServer(mode, null, name);
    }
    
    /**
     * Creates a task that creates a StreamServer using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the server.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<StreamFactory, StreamServer> createStreamServer(
            String sagaFactoryClassname, TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createStreamServer(sagaFactoryClassname, mode, null, name);
    }

    /**
     * Creates a task that creates a StreamServer using the default session.
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
    public static Task<StreamFactory, StreamServer> createStreamServer(
            TaskMode mode) throws NotImplementedException, NoSuccessException {
        return createStreamServer(mode, createDefaultURL());
    }
    

    /**
     * Creates a task that creates a StreamServer using the default session.
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
    public static Task<StreamFactory, StreamServer> createStreamServer(
            String sagaFactoryClassname, TaskMode mode) throws NotImplementedException, NoSuccessException {
        return createStreamServer(sagaFactoryClassname, mode, createDefaultURL());
    }
}
