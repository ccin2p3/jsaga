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

    private static LogicalFileFactory factory;

    private static synchronized void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createLogicalFileFactory();
        }
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
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the file instance.
     */
    public static LogicalFile createLogicalFile(Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateLogicalFile(session, name, flags);
    }

    /**
     * Creates a LogicalFile using READ open mode.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the file instance.
     */
    public static LogicalFile createLogicalFile(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory
                .doCreateLogicalFile(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a LogicalFile using the default session.
     * 
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the file instance.
     */
    public static LogicalFile createLogicalFile(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalFile(session, name, flags);
    }

    /**
     * Creates a LogicalFile using READ open mode, using the default session.
     * 
     * @param name
     *            location of the file.
     * @return the file instance.
     */
    public static LogicalFile createLogicalFile(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory
                .doCreateLogicalFile(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a LogicalDirectory.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the directory instance.
     */
    public static LogicalDirectory createLogicalDirectory(Session session,
            URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateLogicalDirectory(session, name, flags);
    }

    /**
     * Creates a LogicalDirectory using READ open mode.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @return the directory instance.
     */
    public static LogicalDirectory createLogicalDirectory(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateLogicalDirectory(session, name, Flags.READ
                .getValue());
    }

    /**
     * Creates a LogicalDirectory using the default session.
     * 
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the directory instance.
     */
    public static LogicalDirectory createLogicalDirectory(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalDirectory(session, name, flags);
    }

    /**
     * Creates a LogicalDirectory using READ open mode, using the default
     * session.
     * 
     * @param name
     *            location of the directory.
     * @return the directory instance.
     */
    public static LogicalDirectory createLogicalDirectory(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalDirectory(session, name, Flags.READ
                .getValue());
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
        initializeFactory();
        return factory.doCreateLogicalFile(mode, session, name, flags);
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
        initializeFactory();
        return factory.doCreateLogicalFile(mode, session, name, Flags.READ
                .getValue());
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
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalFile(mode, session, name, flags);
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
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalFile(mode, session, name, Flags.READ
                .getValue());
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
        initializeFactory();
        return factory.doCreateLogicalDirectory(mode, session, name, flags);
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
        initializeFactory();
        return factory.doCreateLogicalDirectory(mode, session, name, Flags.READ
                .getValue());
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
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalDirectory(mode, session, name, flags);
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
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateLogicalDirectory(mode, session, name, Flags.READ
                .getValue());
    }
}
