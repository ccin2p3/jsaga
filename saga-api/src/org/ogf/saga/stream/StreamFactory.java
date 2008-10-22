package org.ogf.saga.stream;

import org.ogf.saga.url.URL;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * Factory for objects from the stream package.
 */
public abstract class StreamFactory {
    
    private static StreamFactory factory;

    private static synchronized void initializeFactory()
        throws NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createStreamFactory();
        }
    }

    /**
     * Creates a Stream. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the stream service.
     * @return the stream.
     */
    protected abstract Stream doCreateStream(Session session, URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
                AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                Timeout, NoSuccess;
    
    /**
     * Creates a StreamService. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the service.
     * @return the service.
     */
    protected abstract StreamService doCreateStreamService(Session session,
            URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
                AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                Timeout, NoSuccess;
    
    /**
     * Creates a StreamService. To be provided by the implementation.
     * @param session the session handle.
     * @return the service.
     */
    protected abstract StreamService doCreateStreamService(Session session)
        throws NotImplemented, IncorrectURL, BadParameter,
                AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                Timeout, NoSuccess;
    
    /**
     * Creates a task that creates a Stream.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the stream service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<Stream> doCreateStream(TaskMode mode,
            Session session, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a StreamService.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<StreamService> doCreateStreamService(
            TaskMode mode, Session session, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a StreamService.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<StreamService> doCreateStreamService(
            TaskMode mode, Session session)
        throws NotImplemented;
    
    /**
     * Creates a Stream.
     * @param session the session handle.
     * @param name location of the stream service.
     * @return the stream.
     */
    public static Stream createStream(Session session, URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateStream(session, name);
    }

    /**
     * Creates a StreamService.
     * @param session the session handle.
     * @param name location of the service.
     * @return the service.
     */
    public static StreamService createStreamService(Session session, URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateStreamService(session, name);
    }
    
    /**
     * Creates a StreamService.
     * @param session the session handle.
     * @return the service.
     */
    public static StreamService createStreamService(Session session)
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateStreamService(session);
    }

    /**
     * Creates a Task that creates a Stream.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the stream service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<Stream> createStream(TaskMode mode, Session session,
            URL name) throws NotImplemented {
        initializeFactory();
        return factory.doCreateStream(mode, session, name);
    }

    /**
     * Creates a Task that creates a StreamService.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<StreamService> createStreamService(TaskMode mode,
            Session session, URL name) throws NotImplemented {
        initializeFactory();
        return factory.doCreateStreamService(mode, session, name);
    }
    
    /**
     * Creates a Task that creates a StreamService.
     * @param mode the task mode.
     * @param session the session handle.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<StreamService> createStreamService(TaskMode mode,
            Session session) throws NotImplemented {
        initializeFactory();
        return factory.doCreateStreamService(mode, session);
    }
    
    /**
     * Creates a Stream using the default session.
     * @param name location of the stream service.
     * @return the stream.
     */
    public static Stream createStream(URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStream(session, name);
    }

    /**
     * Creates a StreamService using the default session.
     * @param name location of the service.
     * @return the service.
     */
    public static StreamService createStreamService(URL name)
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(session, name);
    }
    
    /**
     * Creates a StreamService using the default session.
     * @return the service.
     */
    public static StreamService createStreamService()
        throws NotImplemented, IncorrectURL, BadParameter,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(session);
    }

    /**
     * Creates a Task that creates a Stream using the default session.
     * @param mode the task mode.
     * @param name location of the stream service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<Stream> createStream(TaskMode mode, URL name)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStream(mode, session, name);
    }

    /**
     * Creates a Task that creates a StreamService using the default session.
     * @param mode the task mode.
     * @param name location of the service.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<StreamService> createStreamService(TaskMode mode,
            URL name) throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(mode, session, name);
    }
    
    /**
     * Creates a Task that creates a StreamService using the default session.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<StreamService> createStreamService(TaskMode mode)
            throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateStreamService(mode, session);
    }
}
