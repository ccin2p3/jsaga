package org.ogf.saga.file;

import org.ogf.saga.url.URL;
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
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * A Directory instance represents an open directory.
 */
public interface Directory extends NSDirectory {

    // Inspection methods

    /**
     * Returns the number of bytes in the specified file.
     * @param name name of file to inspect.
     * @param flags mode for operation.
     * @return the size.
     */
    public long getSize(URL name, int flags)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Returns the number of bytes in the specified file.
     * @param name name of file to inspect.
     * @return the size.
     */
    public long getSize(URL name)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Tests the name for being a directory entry.
     * Is an alias for {@link NSDirectory#isEntry}.
     * @param name to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFile(URL name)
        throws NotImplemented, IncorrectURL, DoesNotExist, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    // openDirectory and openFile: names changed with respect
    // to specs  because of Java restriction: cannot redefine methods with
    // just a different return type.
    // Thus, they don't hide the methods in NamespaceDirectory, but then,
    // the ones in the SAGA spec don't either, because they have different
    // out parameters.

    /**
     * Creates a new <code>Directory</code> instance.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the opened directory instance.
     */
    public Directory openDirectory(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>Directory</code> instance.
     * @param name directory to open.
     * @return the opened directory instance.
     */
    public Directory openDirectory(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>File</code> instance.
     * @param name file to open.
     * @param flags defining the operation modus.
     * @return the opened file instance.
     */
    public File openFile(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>File</code> instance.
     * @param name file to open.
     * @return the opened file instance.
     */
    public File openFile(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>FileInputStream</code> instance.
     * @param name file to open.
     * @return the input stream.
     */
    public FileInputStream openFileInputStream(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
 
    /**
     * Creates a new <code>FileOutputStream</code> instance.
     * @param name file to open.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStream(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
 
    /**
     * Creates a new <code>FileOutputStream</code> instance.
     * @param name file to open.
     * @param append when set, the stream appends to the file.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStream(URL name, boolean append)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
    
    //
    // Task versions
    //

    /**
     * Creates a task that retrieves the number of bytes in the specified file.
     * @param mode the task mode.
     * @param name name of file to inspect.
     * @param flags mode for operation.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Long> getSize(TaskMode mode, URL name, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that retrieves the number of bytes in the specified file.
     * @param mode the task mode.
     * @param name name of file to inspect.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Long> getSize(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that tests the name for being a directory entry.
     * Is an alias for {@link NSDirectory#isEntry}.
     * @param mode the task mode.
     * @param name to be tested.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isFile(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>Directory</code> instance.
     * @param mode the task mode.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Directory> openDirectory(TaskMode mode, URL name,
            int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>Directory</code> instance.
     * @param mode the task mode.
     * @param name directory to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Directory> openDirectory(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>File</code> instance.
     * @param mode the task mode.
     * @param name file to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<File> openFile(TaskMode mode, URL name, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>File</code> instance.
     * @param mode the task mode.
     * @param name file to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<File> openFile(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>FileInputStream</code> instance.
     * @param mode the task mode.
     * @param name file to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<FileInputStream> openFileInputStream(TaskMode mode, URL name)
        throws NotImplemented;
   
    /**
     * Creates a task that creates a new <code>FileOutputStream</code> instance.
     * @param mode the task mode.
     * @param name file to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<FileOutputStream> openFileOutputStream(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>FileOutputStream</code> instance.
     * @param mode the task mode.
     * @param name file to open.
     * @param append when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<FileOutputStream> openFileOutputStream(TaskMode mode, URL name, boolean append)
        throws NotImplemented;
    
}
