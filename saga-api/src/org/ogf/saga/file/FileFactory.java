package org.ogf.saga.file;

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
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
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
        throws BadParameter, NoSuccess, NotImplemented;

    /**
     * Creates an IOVec. To be provided by the implementation.
     * @param size size of data to be used.
     * @param lenIn number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(int size, int lenIn)
        throws BadParameter, NoSuccess, NotImplemented;
    
    /**
     * Creates an IOVec. To be provided by the implementation.
     * @param data data to be used.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(byte[] data)
        throws BadParameter, NoSuccess, NotImplemented;

    /**
     * Creates an IOVec. To be provided by the implementation.
     * @param size size of data to be used.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(int size)
        throws BadParameter, NoSuccess, NotImplemented;
    
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
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
  
    /**
     * Creates a FileInputStream. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the file.
     * @return the FileInputStream instance.
     */
    protected abstract FileInputStream doCreateFileInputStream(Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a FileOutputStream. To be provided by the implementation.
     * @param session the session handle.
     * @param name location of the file.
     * @param append set when the file is opened for appending.
     * @return the FileOutputStream instance.
     */
    protected abstract FileOutputStream doCreateFileOutputStream(Session session,
            URL name, boolean append)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
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
            BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    
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
     * Creates a Task that creates a FileInputStream. To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<FileInputStream> doCreateFileInputStream(TaskMode mode,
            Session session, URL name)
        throws NotImplemented;
    
    /**
     * Creates a Task that creates a FileOutputStream. To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @param append when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<FileOutputStream> doCreateFileOutputStream(TaskMode mode,
            Session session, URL name, boolean append)
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
     * @throws NotImplemented 
     */
    public static IOVec createIOVec(byte[] data, int lenIn)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateIOVec(data, lenIn);
    }
    
    /**
     * Creates an IOVec.
     * @param data data to be used.
     * @return the IOVec.
     * @throws NotImplemented 
     */
    public static IOVec createIOVec(byte[] data)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateIOVec(data);
    }
    
    /**
     * Creates an IOVec.
     * @param size size of data to be used.
     * @param lenIn number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     * @throws NotImplemented 
     */
    public static IOVec createIOVec(int size, int lenIn)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateIOVec(size, lenIn);
    }
    
    /**
     * Creates an IOVec.
     * @param size size of data to be used.
     * @return the IOVec.
     * @throws NotImplemented 
     */
    public static IOVec createIOVec(int size)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateIOVec(size);
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
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateFile(session, name, flags);
    }

    /**
     * Creates a FileInputStream.
     * @param session the session handle.
     * @param name location of the file.
     * @return the FileInputStream instance.
     */
    public static FileInputStream createFileInputStream(Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateFileInputStream(session, name);
    }

    /**
     * Creates a FileOutputStream.
     * @param session the session handle.
     * @param name location of the file.
     * @param append when set, the file is opened for appending.
     * @return the FileOutputStream instance.
     */
    public static FileOutputStream createFileOutputStream(Session session, URL name,
            boolean append)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateFileOutputStream(session, name, append);
    }

    /**
     * Creates a FileOutputStream.
     * @param session the session handle.
     * @param name location of the file.
     * @return the FileOutputStream instance.
     */
    public static FileOutputStream createFileOutputStream(Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        return createFileOutputStream(session, name, false);
    }

    /**
     * Creates a File for reading.
     * @param session the session handle.
     * @param name location of the file.
     * @return the file instance.
     */
    public static File createFile(Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateFile(session, name, Flags.READ.getValue());
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
            BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateDirectory(session, name, flags);
    }
    
    /**
     * Creates a Directory for reading.
     * @param session the session handle.
     * @param name location of the directory.
     * @return the directory instance.
     */
    public static Directory createDirectory(Session session, URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateDirectory(session, name, Flags.READ.getValue());
    }
    
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
     * Creates a Task that creates a File for reading.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<File> createFile(TaskMode mode,
            Session session, URL name)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateFile(mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a Task that creates a FileInputStream.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<FileInputStream> createFileInputStream(TaskMode mode,
            Session session, URL name)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateFileInputStream(mode, session, name);
    }
 
    /**
     * Creates a Task that creates a FileOutputStream.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<FileOutputStream> createFileOutputStream(TaskMode mode,
            Session session, URL name)
        throws NotImplemented {
        return createFileOutputStream(mode, session, name, false);
    }
   
    /**
     * Creates a Task that creates a FileOutputStream.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the file.
     * @param append when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<FileOutputStream> createFileOutputStream(TaskMode mode,
            Session session, URL name, boolean append)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateFileOutputStream(mode, session, name, append);
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

    /**
     * Creates a Task that creates a Directory for reading.
     * @param mode the task mode.
     * @param session the session handle.
     * @param name location of the directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<Directory> createDirectory(TaskMode mode,
            Session session, URL name)
        throws NotImplemented {
        initializeFactory();
        return factory.doCreateDirectory(mode, session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a File using the default session.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the file instance.
     */
    public static File createFile(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateFile(session, name, flags);
    }

    /**
     * Creates a File for reading, using the default session.
     * @param name location of the file.
     * @return the file instance.
     */
    public static File createFile(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateFile(session, name, Flags.READ.getValue());
    }

    /**
     * Creates a Directory, using the default session.
     * @param name location of the directory.
     * @param flags the open mode.
     * @return the directory instance.
     */
    public static Directory createDirectory(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateDirectory(session, name, flags);
    }
    
    /**
     * Creates a Directory for reading, using the default session.
     * @param name location of the directory.
     * @return the directory instance.
     */
    public static Directory createDirectory(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateDirectory(session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a Task that creates a File, using the default session.
     * @param mode the task mode.
     * @param name location of the file.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<File> createFile(TaskMode mode, URL name, int flags)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateFile(mode, session, name, flags);
    }
    
    /**
     * Creates a Task that creates a File for reading, using the default session.
     * @param mode the task mode.
     * @param name location of the file.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<File> createFile(TaskMode mode, URL name)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateFile(mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a Task that creates a Directory, using the default session.
     * @param mode the task mode.
     * @param name location of the directory.
     * @param flags the open mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<Directory> createDirectory(TaskMode mode,
            URL name, int flags)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateDirectory(mode, session, name, flags);
    }

    /**
     * Creates a Task that creates a Directory for reading, using the default session.
     * @param mode the task mode.
     * @param name location of the directory.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<Directory> createDirectory(TaskMode mode, URL name)
        throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateDirectory(mode, session, name, Flags.READ.getValue());
    }
}

