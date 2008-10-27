package org.ogf.saga.logicalfile;

import java.util.List;

import org.ogf.saga.attributes.AsyncAttributes;
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
 * This interface represents a container for logical files in a logical file
 * name space.
 */
public interface LogicalDirectory extends NSDirectory,
        AsyncAttributes<LogicalDirectory> {

    /**
     * Tests the name for being a logical file. Is an alias for
     * {@link NSDirectory#isEntry}.
     * 
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFile(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, DoesNotExistException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Finds entries in the current directory and possibly below, with matching
     * names and matching meta data.
     * 
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @param flags
     *            flags defining the operation modus.
     * @return the list of matching entries.
     */
    public List<URL> find(String namePattern, String[] attrPattern, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Finds entries in the current directory and below, with matching names and
     * matching meta data.
     * 
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @return the list of matching entries.
     */
    public List<URL> find(String namePattern, String[] attrPattern)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    // openLogicalDir and openLogicalFile: names changed with respect
    // to specs because of Java restriction: cannot redefine methods with
    // just a different return type.

    /**
     * Creates a new <code>LogicalDirectory</code> instance.
     * 
     * @param name
     *            directory to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened directory instance.
     */
    public LogicalDirectory openLogicalDir(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>LogicalDirectory</code> instance with read flag.
     * 
     * @param name
     *            directory to open.
     * @return the opened directory instance.
     */
    public LogicalDirectory openLogicalDir(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>LogicalFile</code> instance.
     * 
     * @param name
     *            logical file to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened logical file.
     */
    public LogicalFile openLogicalFile(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>LogicalFile</code> instance with read flag.
     * 
     * @param name
     *            logical file to open.
     * @return the opened logical file.
     */
    public LogicalFile openLogicalFile(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    // Task versions ...

    /**
     * Creates a task that tests the name for being a logical file. Is an alias
     * for {@link NSDirectory#isEntry}.
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
    public Task<NSDirectory, Boolean> isFile(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that finds entries in the current directory and below,
     * with matching names and matching meta data.
     * 
     * @param mode
     *            the task mode.
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @param flags
     *            flags defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalDirectory, List<URL>> find(TaskMode mode,
            String namePattern, String[] attrPattern, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that finds entries in the current directory and below,
     * with matching names and matching meta data.
     * 
     * @param mode
     *            the task mode.
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalDirectory, List<URL>> find(TaskMode mode,
            String namePattern, String[] attrPattern)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>LogicalDirectory</code>
     * instance.
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
    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(
            TaskMode mode, URL name, int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>LogicalDirectory</code>
     * instance.
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
    public Task<LogicalDirectory, LogicalDirectory> openLogicalDir(
            TaskMode mode, URL name) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>LogicalFile</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the file to open.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode,
            URL name, int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>LogicalFile</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the file to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalDirectory, LogicalFile> openLogicalFile(TaskMode mode,
            URL name) throws NotImplementedException;
}
