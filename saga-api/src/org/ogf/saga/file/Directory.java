package org.ogf.saga.file;

import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

/**
 * A Directory instance represents an open directory.
 */
public interface Directory extends NSDirectory {

    // Inspection methods

    /**
     * Returns the number of bytes in the specified file.
     * 
     * @param name
     *            name of file to inspect.
     * @param flags
     *            mode for operation.
     * @return the size.
     */
    public long getSize(URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Returns the number of bytes in the specified file.
     * 
     * @param name
     *            name of file to inspect.
     * @return the size.
     */
    public long getSize(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Tests the name for being a directory entry. Is an alias for
     * {@link NSDirectory#isEntry}.
     * 
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFile(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    // openDirectory and openFile: names changed with respect
    // to specs because of Java restriction: cannot redefine methods with
    // just a different return type.
    // Thus, they don't hide the methods in NamespaceDirectory, but then,
    // the ones in the SAGA spec don't either, because they have different
    // out parameters.

    /**
     * Creates a new <code>Directory</code> instance.
     * 
     * @param name
     *            directory to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened directory instance.
     */
    public Directory openDirectory(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>Directory</code> instance.
     * 
     * @param name
     *            directory to open.
     * @return the opened directory instance.
     */
    public Directory openDirectory(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>File</code> instance.
     * 
     * @param name
     *            file to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened file instance.
     */
    public File openFile(URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>File</code> instance.
     * 
     * @param name
     *            file to open.
     * @return the opened file instance.
     */
    public File openFile(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>FileInputStream</code> instance.
     * 
     * @param name
     *            file to open.
     * @return the input stream.
     */
    public FileInputStream openFileInputStream(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>FileOutputStream</code> instance.
     * 
     * @param name
     *            file to open.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStream(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>FileOutputStream</code> instance.
     * 
     * @param name
     *            file to open.
     * @param append
     *            when set, the stream appends to the file.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStream(URL name, boolean append)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    //
    // Task versions
    //

    /**
     * Creates a task that retrieves the number of bytes in the specified file.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            name of file to inspect.
     * @param flags
     *            mode for operation.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, Long> getSize(TaskMode mode, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that retrieves the number of bytes in the specified file.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            name of file to inspect.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, Long> getSize(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that tests the name for being a directory entry. Is an
     * alias for {@link NSDirectory#isEntry}.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            to be tested.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, Boolean> isFile(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>Directory</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            directory to open.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, Directory> openDirectory(TaskMode mode, URL name,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>Directory</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            directory to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, Directory> openDirectory(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>File</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            file to open.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, File> openFile(TaskMode mode, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>File</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            file to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, File> openFile(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>FileInputStream</code>
     * instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            file to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, FileInputStream> openFileInputStream(TaskMode mode,
            URL name) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>FileOutputStream</code>
     * instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            file to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, FileOutputStream> openFileOutputStream(
            TaskMode mode, URL name) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>FileOutputStream</code>
     * instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            file to open.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<Directory, FileOutputStream> openFileOutputStream(
            TaskMode mode, URL name, boolean append)
            throws NotImplementedException;

}
