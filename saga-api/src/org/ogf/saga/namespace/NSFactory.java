package org.ogf.saga.namespace;

import org.ogf.saga.URL;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
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
 * Factory for objects from the namespace package.
 */
public abstract class NSFactory {
    
    private static NSFactory factory;

    private static synchronized void initializeFactory()
        throws NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createNamespaceFactory();
        }
    }
    
    /**
     * Creates a task that creates a namespace entry.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<NSEntry> doCreateNSEntry(
            TaskMode mode, Session session, URL name, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a namespace directory.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<NSDirectory>
            doCreateNSDirectory(TaskMode mode, Session session, URL name,
                    int flags)
            throws NotImplemented;
    
    
    /**
     * Creates a namespace entry. To be provided by the implementation.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace entry.
     */
    protected abstract NSEntry doCreateNSEntry(
            Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess;
    
    /**
     * Creates a namespace directory. To be provided by the implementation.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace directory.
     */
    protected abstract NSDirectory doCreateNSDirectory(
            Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess;
    
    /**
     * Creates a namespace entry.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace entry.
     */
    public static NSEntry createNSEntry(
            Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateNSEntry(session, name, flags);
    }
    
    /**
     * Creates a namespace entry.
     * @param session the session handle.
     * @param name the initial working directory.
     * @return the namespace entry.
     */
    public static NSEntry createNSEntry(
            Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateNSEntry(session, name, Flags.NONE.getValue());
    }

    /**
     * Creates a namespace directory.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace entry.
     */
    public static NSDirectory createNSDirectory(
            Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, flags);
    }
    
    /**
     * Creates a namespace directory.
     * @param session the session handle.
     * @param name the initial working directory.
     * @return the namespace entry.
     */
    public static NSDirectory createNSDirectory(
            Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, Flags.NONE.getValue());
    }

    /**
     * Creates a task that creates a namespace entry.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<NSEntry> createNSEntry(TaskMode mode,
            Session session, URL name, int flags)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a namespace entry.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<NSEntry> createNSEntry(TaskMode mode,
            Session session, URL name)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, Flags.NONE.getValue());
    }


    /**
     * Creates a task that creates a namespace directory.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    
    public static Task<NSDirectory> createNSDirectory(
            TaskMode mode, Session session, URL name, int flags)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a namespace directory.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name the initial working directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<NSDirectory> createNSDirectory(
            TaskMode mode, Session session, URL name)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, Flags.NONE.getValue());
    }
    
    
    
    /**
     * Creates a namespace entry using the default session.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace entry.
     */
    public static NSEntry createNSEntry(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        Session session = SessionFactory.createSession(); 
        initializeFactory();
        return factory.doCreateNSEntry(session, name, flags);
    }
    
    /**
     * Creates a namespace entry using the default session.
     * @param name the initial working directory.
     * @return the namespace entry.
     */
    public static NSEntry createNSEntry(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(session, name, Flags.NONE.getValue());
    }

    /**
     * Creates a namespace directory using the default session.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the namespace entry.
     */
    public static NSDirectory createNSDirectory(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, flags);
    }
    
    /**
     * Creates a namespace directory using the default session.
     * @param name the initial working directory.
     * @return the namespace entry.
     */
    public static NSDirectory createNSDirectory(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(session, name, Flags.NONE.getValue());
    }

    /**
     * Creates a task that creates a namespace entry using the default session.
     * @param mode the task mode.
      * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<NSEntry> createNSEntry(TaskMode mode, URL name, int flags)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a namespace entry using the default session.
     * @param mode the task mode.
     * @param name the initial working directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<NSEntry> createNSEntry(TaskMode mode, URL name)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSEntry(mode, session, name, Flags.NONE.getValue());
    }


    /**
     * Creates a task that creates a namespace directory using the default session.
     * @param mode the task mode.
     * @param name the initial working directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    
    public static Task<NSDirectory> createNSDirectory(
            TaskMode mode, URL name, int flags)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, flags);
    }
    
    /**
     * Creates a task that creates a namespace directory using the default session.
     * @param mode the task mode.
     * @param name the initial working directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<NSDirectory> createNSDirectory(TaskMode mode, URL name)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateNSDirectory(mode, session, name, Flags.NONE.getValue());
    }
}
