package org.ogf.saga.stream;

import org.ogf.saga.URI;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
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
    protected abstract Stream doCreateStream(Session session, URI name)
        throws NotImplemented, IncorrectURL, IncorrectSession,
                AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                Timeout, NoSuccess;
    
    /**
     * Creates a StreamService. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the service.
     * @return the service.
     */
    protected abstract StreamService doCreateStreamService(Session session,
            URI name)
        throws NotImplemented, IncorrectURL, IncorrectSession,
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
    protected abstract RVTask<Stream> doCreateStream(TaskMode mode,
            Session session, URI name)
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
    protected abstract RVTask<StreamService> doCreateStreamService(
            TaskMode mode, Session session, URI name)
        throws NotImplemented;
    
    /**
     * Creates a Stream.
     * @param session the session handle.
     * @param name location of the stream service.
     * @return the stream.
     */
    public static Stream createStream(Session session, URI name)
        throws NotImplemented, IncorrectURL, IncorrectSession,
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
    public static StreamService createStreamService(Session session, URI name)
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateStreamService(session, name);
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
    public static RVTask<Stream> createStream(TaskMode mode, Session session,
            URI name) throws NotImplemented {
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
    public static RVTask<StreamService> createStreamService(TaskMode mode,
            Session session, URI name) throws NotImplemented {
        initializeFactory();
        return factory.doCreateStreamService(mode, session, name);
    }
}
