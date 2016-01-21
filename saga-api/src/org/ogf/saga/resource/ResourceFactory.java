package org.ogf.saga.resource;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public abstract class ResourceFactory {

    private static ResourceFactory getFactory(String sagaFactoryName)
            throws NoSuccessException, NotImplementedException {
        return ImplementationBootstrapLoader.getManagerFactory(sagaFactoryName);
    }

    /**
     * Creates a compute resource description. To be provided by the implementation.
     *
     * @return the compute resource description.
     */
    protected abstract ComputeDescription doCreateComputeDescription()
            throws NotImplementedException, NoSuccessException;

    /**
     * Creates a network resource description. To be provided by the implementation.
     *
     * @return the network resource description.
     */
    protected abstract NetworkDescription doCreateNetworkDescription()
            throws NotImplementedException, NoSuccessException;

    /**
     * Creates a storage resource description. To be provided by the implementation.
     *
     * @return the storage resource description.
     */
    protected abstract StorageDescription doCreateStorageDescription()
            throws NotImplementedException, NoSuccessException;

    /**
     * Creates a resource manager. To be provided by the implementation.
     *
     * @param session
     *            the session handle.
     * @param rm
     *            contact string for the resource manager.
     * @return the resource manager.
     */
    protected abstract ResourceManager doCreateResourceManager(Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a task that creates a resource manager. To be provided by the
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
     *      is thrown when the task version of this method is not implemented.
     */
    protected abstract Task<ResourceFactory, ResourceManager> doCreateResourceManager(
            TaskMode mode, Session session, URL rm)
            throws NotImplementedException;

    /**
     * Creates a resource description.
     *
     * @return
     *      the resource description.
     * @exception NotImplementedException
     *      is thrown when this method is not implemented.
     * @throws NoSuccessException
     *      is thrown when the Saga factory could not be created.
     */
    public static ResourceDescription createResourceDescription(Type type)
            throws NotImplementedException, BadParameterException, NoSuccessException {
        return createResourceDescription(null, type);
    }

    /**
     * Creates a resource description.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the resource description.
     * @exception NotImplementedException
     *      is thrown when this method is not implemented.
     * @throws NoSuccessException
     *      is thrown when the Saga factory could not be created.
     */
    public static ResourceDescription createResourceDescription(String sagaFactoryClassname, Type type)
            throws NotImplementedException, BadParameterException, NoSuccessException {
        ResourceFactory factory = getFactory(sagaFactoryClassname);
        if (type != null) {
            switch (type) {
                case COMPUTE:
                    return factory.doCreateComputeDescription();
                case NETWORK:
                    return factory.doCreateNetworkDescription();
                case STORAGE:
                    return factory.doCreateStorageDescription();
                default:
                    throw new BadParameterException("Unexpected resource type: "+type.name());
            }
        } else {
            throw new BadParameterException("Missing required argument: "+Type.class.getSimpleName());
        }
    }

    /**
     * Creates a resource manager.
     *
     * @param session
     *      the session handle.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        return createResourceManager((String) null, session, rm);
    }

    /**
     * Creates a resource manager.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(String sagaFactoryClassname, Session session, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        if (session == null) {
            session = SessionFactory.createSession(sagaFactoryClassname);
        }
        if (rm == null) {
            rm = URLFactory.createURL(sagaFactoryClassname, "");
        }
        return getFactory(sagaFactoryClassname).doCreateResourceManager(session, rm);
    }

    /**
     * Creates a resource manager using the default contact string.
     *
     * @param session
     *      the session handle.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(Session session)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        return createResourceManager(session, (URL) null);
    }

    /**
     * Creates a resource manager using the default contact string.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(String sagaFactoryClassname, Session session)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        return createResourceManager(sagaFactoryClassname, session, (URL) null);
    }

    /**
     * Creates a resource manager, using the default session.
     *
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        return createResourceManager((Session) null, rm);
    }

    /**
     * Creates a resource manager, using the default session.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param rm
     *      contact string for the resource manager.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(String sagaFactoryClassname, URL rm)
            throws NotImplementedException, BadParameterException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        return createResourceManager(sagaFactoryClassname, (Session) null, rm);
    }

    /**
     * Creates a resource manager, using the default session and default contact
     * string.
     *
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager() throws NotImplementedException,
            BadParameterException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, TimeoutException, NoSuccessException {
        return createResourceManager((Session) null);
    }

    /**
     * Creates a resource manager, using the default session and default contact
     * string.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the resource manager.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
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
    public static ResourceManager createResourceManager(String sagaFactoryClassname) throws NotImplementedException,
            BadParameterException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, TimeoutException, NoSuccessException {
        return createResourceManager(sagaFactoryClassname, (Session) null);
    }

    /**
     * Creates a task that creates a resource manager.
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(TaskMode mode,
                                                                               Session session, URL rm) throws NotImplementedException,
            NoSuccessException {
        return createResourceManager((String) null, mode, session, rm);
    }

    /**
     * Creates a task that creates a resource manager.
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(String sagaFactoryClassname, TaskMode mode,
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
        return getFactory(sagaFactoryClassname).doCreateResourceManager(mode, session, rm);
    }

    /**
     * Creates a task that creates a resource manager, using a default contact
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(TaskMode mode,
                                                                               Session session) throws NotImplementedException, NoSuccessException {
        return createResourceManager(mode, session, (URL) null);
    }

    /**
     * Creates a task that creates a resource manager, using a default contact
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(String sagaFactoryClassname, TaskMode mode,
                                                                               Session session) throws NotImplementedException, NoSuccessException {
        return createResourceManager(sagaFactoryClassname, mode, session, (URL) null);
    }

    /**
     * Creates a task that creates a resource manager, using the default session.
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(TaskMode mode,
                                                                               URL rm) throws NotImplementedException, NoSuccessException {
        return createResourceManager(mode, (Session) null, rm);
    }

    /**
     * Creates a task that creates a resource manager, using the default session.
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(String sagaFactoryClassname, TaskMode mode,
                                                                               URL rm) throws NotImplementedException, NoSuccessException {
        return createResourceManager(sagaFactoryClassname, mode, (Session) null, rm);
    }

    /**
     * Creates a task that creates a resource manager, using the default session and
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(TaskMode mode)
            throws NotImplementedException, NoSuccessException {
        return createResourceManager(mode, (URL) null);
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
    public static Task<ResourceFactory, ResourceManager> createResourceManager(String sagaFactoryClassname, TaskMode mode)
            throws NotImplementedException, NoSuccessException {
        return createResourceManager(sagaFactoryClassname, mode, (URL) null);
    }
}
