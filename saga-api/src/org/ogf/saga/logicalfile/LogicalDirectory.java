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
     *      to be tested.
     * @return
     *      <code>true</code> if the name represents a non-directory entry.
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
     *      is thrown when the specified URL contains an invalid entry name.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception DoesNotExistException
     *      is thrown if the specified name does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
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
     *      pattern for names of entries to be found.
     * @param attrPattern
     *      pattern for meta data keys/values of entries to be found.
     * @param flags
     *      flags defining the operation modus.
     * @return
     *      the list of matching entries.
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
     *      is thrown when illegal flags are specified: only RECURSIVE
     *      (or NONE) is allowed, or one or more of the patterns is
     *      not correctly formatted.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
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
     *      pattern for names of entries to be found.
     * @param attrPattern
     *      pattern for meta data keys/values of entries to be found.
     * @return
     *      the list of matching entries.
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
     *      is thrown when one or more of the patterns is
     *      not correctly formatted.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
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
     *      directory to open.
     * @param flags
     *      defining the operation modus.
     * @return
     *      the opened directory instance.
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
     *      is thrown when the specified URL does not point to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
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
     *      directory to open.
     * @return
     *      the opened directory instance.
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
     *      is thrown when the specified URL does not point to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      not thrown, but specified because a method may be invoked
     *      that can throw this exception, but will not in this case.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
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
     *      logical file to open.
     * @param flags
     *      defining the operation modus.
     * @return
     *      the opened logical file.
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
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
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
     *      is thrown when the specified URL does not point to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectStateException
     *      is thrown when the LogicalDirectory is already closed.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      not thrown, but specified because a method may be invoked
     *      that can throw this exception, but will not in this case.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
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
