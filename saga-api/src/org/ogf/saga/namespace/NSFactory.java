package org.ogf.saga.namespace;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
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
 * Factory for objects from the namespace package.
 */
public abstract class NSFactory {

    private static NSFactory factory;

    private static synchronized void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createNamespaceFactory();
        }
    }

    /**
     * Creates a task that creates a namespace entry. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<NSFactory, NSEntry> doCreateNSEntry(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a namespace directory. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<NSFactory, NSDirectory> doCreateNSDirectory(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a namespace entry. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the namespace entry.
     */
    protected abstract NSEntry doCreateNSEntry(Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a namespace directory. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the namespace directory.
     */
    protected abstract NSDirectory doCreateNSDirectory(Session session,
            URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, DoesNotExistException,
            AlreadyExistsException, TimeoutException, NoSuccessException;

    /**
     * Creates a namespace entry.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      the initial working directory.
     * @param flags
     *      the open mode.
     * @return
     *      the namespace entry.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSEntry createNSEntry(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateNSEntry(session, name, flags);
    }

    /**
     * Creates a namespace entry.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      the initial working directory.
     * @return
     *      the namespace entry.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSEntry createNSEntry(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateNSEntry(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a namespace entry using the default session.
     * 
     * @param name
     *      the initial working directory.
     * @param flags
     *      the open mode.
     * @return
     *      the namespace entry.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSEntry createNSEntry(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(session, name, flags);
    }

    /**
     * Creates a namespace entry using the default session.
     * 
     * @param name
     *      the initial working directory.
     * @return
     *      the namespace entry.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSEntry createNSEntry(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a namespace directory.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      the initial working directory.
     * @param flags
     *      the open mode.
     * @return
     *      the namespace directory.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSDirectory createNSDirectory(Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, flags);
    }

    /**
     * Creates a namespace directory.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      the initial working directory.
     * @return
     *      the namespace directory.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSDirectory createNSDirectory(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory
                .doCreateNSDirectory(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a namespace directory using the default session.
     * 
     * @param name
     *      the initial working directory.
     * @param flags
     *      the open mode.
     * @return
     *      the namespace directory.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSDirectory createNSDirectory(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, flags);
    }

    /**
     * Creates a namespace directory using the default session.
     * 
     * @param name
     *      the initial working directory.
     * @return 
     *      the namespace directory.
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
     *      is thrown when the specified URL is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static NSDirectory createNSDirectory(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, AlreadyExistsException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory
                .doCreateNSDirectory(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a namespace entry.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSEntry> createNSEntry(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a namespace entry.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSEntry> createNSEntry(TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name,
                Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a namespace entry using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSEntry> createNSEntry(TaskMode mode,
            URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a namespace entry using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the initial working directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSEntry> createNSEntry(TaskMode mode, URL name)
            throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name,
                Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a namespace directory.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */

    public static Task<NSFactory, NSDirectory> createNSDirectory(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a namespace directory.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            the initial working directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSDirectory> createNSDirectory(TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name,
                Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a namespace directory using the default
     * session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the initial working directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */

    public static Task<NSFactory, NSDirectory> createNSDirectory(TaskMode mode,
            URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a namespace directory using the default
     * session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the initial working directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<NSFactory, NSDirectory> createNSDirectory(TaskMode mode,
            URL name) throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name,
                Flags.READ.getValue());
    }
}
