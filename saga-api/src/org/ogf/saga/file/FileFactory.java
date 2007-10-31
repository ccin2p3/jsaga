package org.ogf.saga.file;

import org.ogf.saga.URL;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * Factory for objects from the namespace package.
 */
public abstract class FileFactory {
    
    private static FileFactory factory;

    private static synchronized void initializeFactory() {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createFileFactory();
        }
    }

    /**
     * Creates an IOVec. To be provided by the implementation.
     * @param data data to be used.
     * @param lenIn number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(byte[] data, int lenIn)
        throws BadParameter;
    
    /**
     * Creates a File. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the file instance.
     */
    protected abstract File doCreateFile(Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
    
    /**
     * Creates a Directory. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of directory.
     * @param flags the open mode.
     * @return the directory instance.
     */
    protected abstract Directory doCreateDirectory(Session session, URL name,
            int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
    //sreynaud: IncorrectState added for consistency with method doCreateFile
    
    /**
     * Creates a Task that creates a File. To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<File> doCreateFile(TaskMode mode,
            Session session, URL name, int flags)
        throws NotImplemented;
    
    /**
     * Creates a Task that creates a Directory.
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<Directory> doCreateDirectory(
            TaskMode mode, Session session, URL name, int flags)
        throws NotImplemented;
    
    /**
     * Creates an IOVec.
     * @param data data to be used.
     * @param lenIn number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     */
    public static IOVec createIOVec(byte[] data, int lenIn)
        throws BadParameter {
        initializeFactory();
        return factory.doCreateIOVec(data, lenIn);
    }
    
    /**
     * Creates a File.
     * @param session the session handle.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the file instance.
     */
    public static File createFile(Session session, URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateFile(session, name, flags);
    }

    /**
     * Creates a Directory.
     * @param session the session handle.
     * @param name location of the directory.
     * @param flags the open mode.
     * @return the directory instance.
     */
    public static Directory createDirectory(Session session, URL name,
            int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateDirectory(session, name, flags);
    }
    //sreynaud: IncorrectState added for consistency with method doCreateFile
    
    /**
     * Creates a Task that creates a File.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<File> createFile(TaskMode mode,
            Session session, URL name, int flags)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateFile(mode, session, name, flags);
    }

    /**
     * Creates a Task that creates a Directory.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<Directory> createDirectory(TaskMode mode,
            Session session, URL name, int flags)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateDirectory(mode, session, name, flags);
    }
}
