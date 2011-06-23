package org.ogf.saga.logicalfile;

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
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

/**
 * Factory for objects from the logicalfile package.
 */
public abstract class LogicalFileFactory {
    
    private static LogicalFileFactory getFactory(String sagaFactoryName)
            throws NoSuccessException, NotImplementedException {
	return ImplementationBootstrapLoader.getLogicalFileFactory(sagaFactoryName);
    }

    /**
     * Creates a LogicalFile. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the file instance.
     */
    protected abstract LogicalFile doCreateLogicalFile(Session session,
            URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a Directory. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of directory.
     * @param flags
     *            the open mode.
     * @return the directory instance.
     */
    protected abstract LogicalDirectory doCreateLogicalDirectory(
            Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a task that creates a LogicalFile. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<LogicalFileFactory, LogicalFile> doCreateLogicalFile(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a LogicalDirectory. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<LogicalFileFactory, LogicalDirectory> doCreateLogicalDirectory(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a LogicalFile.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
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
    public static LogicalFile createLogicalFile(Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile((String) null, session, name, flags);
    }
    

    /**
     * Creates a LogicalFile.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
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
    public static LogicalFile createLogicalFile(String sagaFactoryClassname, Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateLogicalFile(session, name, flags);
    }


    /**
     * Creates a LogicalFile using READ open mode.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalFile createLogicalFile(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile(session, name, Flags.READ.getValue());
    }
    

    /**
     * Creates a LogicalFile using READ open mode.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalFile createLogicalFile(String sagaFactoryClassname, Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a LogicalFile using the default session.
     * 
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
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
    public static LogicalFile createLogicalFile(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile((Session) null, name, flags);
    }
    
    /**
     * Creates a LogicalFile using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
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
    public static LogicalFile createLogicalFile(String sagaFactoryClassname, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, (Session) null, name, flags);
    }

    /**
     * Creates a LogicalFile using READ open mode, using the default session.
     * 
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalFile createLogicalFile(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile((Session) null, name);
    }
    
    /**
     * Creates a LogicalFile using READ open mode, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalFile createLogicalFile(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, (Session) null, name);
    }


    /**
     * Creates a LogicalDirectory.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
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
    public static LogicalDirectory createLogicalDirectory(Session session,
            URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return createLogicalDirectory((String) null, session, name, flags);
    }
    

    /**
     * Creates a LogicalDirectory.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
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
    public static LogicalDirectory createLogicalDirectory(String sagaFactoryClassname, Session session,
            URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateLogicalDirectory(session, name, flags);
    }

    /**
     * Creates a LogicalDirectory using READ open mode.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalDirectory createLogicalDirectory(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory(session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a LogicalDirectory using READ open mode.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalDirectory createLogicalDirectory(String sagaFactoryClassname, Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, session, name, Flags.READ.getValue());
    }


    /**
     * Creates a LogicalDirectory using the default session.
     * 
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
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
    public static LogicalDirectory createLogicalDirectory(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory((Session) null, name, flags);
    }

    /**
     * Creates a LogicalDirectory using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
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
    public static LogicalDirectory createLogicalDirectory(String sagaFactoryClassname, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, (Session) null, name, flags);
    }


    /**
     * Creates a LogicalDirectory using READ open mode, using the default
     * session.
     * 
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalDirectory createLogicalDirectory(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory(name, Flags.READ.getValue());
    }
    
    /**
     * Creates a LogicalDirectory using READ open mode, using the default
     * session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
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
     *      is actually not thrown, but specified in the SAGA specifications.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static LogicalDirectory createLogicalDirectory(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, name, Flags.READ.getValue());
    }


    /**
     * Creates a task that creates a LogicalFile.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        return createLogicalFile((String) null, mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a LogicalFile.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateLogicalFile(mode, session, name, flags);
    }


    /**
     * Creates a task that creates a LogicalFile using READ open mode.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createLogicalFile(mode, session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a LogicalFile using READ open mode.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a LogicalFile using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
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
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            TaskMode mode, URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        return createLogicalFile(mode, (Session) null, name, flags);
    }
    
    /**
     * Creates a task that creates a LogicalFile using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
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
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            String sagaFactoryClassname, TaskMode mode, URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, mode, (Session) null, name, flags);
    }

    /**
     * Creates a task that creates a LogicalFile using READ open mode, using the
     * default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createLogicalFile(mode, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a LogicalFile using READ open mode, using the
     * default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalFile> createLogicalFile(
            String sagaFactoryClassname, TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createLogicalFile(sagaFactoryClassname, mode, name, Flags.READ.getValue());
    }


    /**
     * Creates a task that creates a LogicalDirectory.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        return createLogicalDirectory((String) null, mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a LogicalDirectory.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateLogicalDirectory(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a LogicalDirectory using READ open mode.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createLogicalDirectory(mode, session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a LogicalDirectory using READ open mode.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a LogicalDirectory using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
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
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            TaskMode mode, URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        return createLogicalDirectory(mode, (Session) null, name, flags);
    }
    
    /**
     * Creates a task that creates a LogicalDirectory using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
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
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            String sagaFactoryClassname, TaskMode mode, URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, mode, (Session) null, name, flags);
    }

    /**
     * Creates a task that creates a LogicalDirectory using READ open mode,
     * using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createLogicalDirectory(mode, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a LogicalDirectory using READ open mode,
     * using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<LogicalFileFactory, LogicalDirectory> createLogicalDirectory(
            String sagaFactoryClassname, TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createLogicalDirectory(sagaFactoryClassname, mode, name, Flags.READ.getValue());
    }
}
