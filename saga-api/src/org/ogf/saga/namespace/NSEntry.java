package org.ogf.saga.namespace;

import org.ogf.saga.SagaObject;
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
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/**
 * Defines methods that allow inspection and management of the entry.
 */
public interface NSEntry extends SagaObject, Async, Permissions<NSEntry> {

    /**
     * Obtains the complete URL refering to the entry.
     * 
     * @return the URL.
     */
    public URL getURL() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the current working directory for the entry.
     * 
     * @return the current working directory.
     */
    public URL getCWD() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the name part of the URL of this entry.
     * 
     * @return the name part.
     */
    public URL getName() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a directory.
     * 
     * @return true if the entry is a directory.
     */
    public boolean isDir() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a namespace entry. If this entry represents a
     * link or a directory, this method returns <code>false</code>, although
     * strictly speaking, directories and links are namespace entries as well.
     * 
     * @return true if the entry is a namespace entry.
     */
    public boolean isEntry() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a link.
     * 
     * @return true if the entry is a link.
     */
    public boolean isLink() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Returns the URL representing the link target. Resolves one link level
     * only.
     * 
     * @return the link target.
     */
    public URL readLink() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Copies this entry to another part of the namespace.
     * 
     * @param target
     *            the name to copy to.
     * @param flags
     *            defining the operation modus.
     */
    public void copy(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Copies this entry to another part of the namespace.
     * 
     * @param target
     *            the name to copy to.
     */
    public void copy(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Creates a symbolic link from the target to this entry.
     * 
     * @param target
     *            the name that will have the symbolic link to this entry.
     * @param flags
     *            defining the operation modus.
     */
    public void link(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException, TimeoutException,
            NoSuccessException, IncorrectURLException;

    /**
     * Creates a symbolic link from the target to this entry.
     * 
     * @param target
     *            the name that will have the symbolic link to this entry.
     */
    public void link(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException, TimeoutException,
            NoSuccessException, IncorrectURLException;

    /**
     * Renames this entry to the target, or moves this entry to the target if it
     * is a directory.
     * 
     * @param target
     *            the name to move to.
     * @param flags
     *            defining the operation modus.
     */
    public void move(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Renames this entry to the target, or moves this entry to the target if it
     * is a directory.
     * 
     * @param target
     *            the name to move to.
     */
    public void move(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Removes this entry and closes it.
     * 
     * @param flags
     *            defining the operation modus.
     */
    public void remove(int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Removes this entry and closes it.
     */
    public void remove() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Closes this entry. This is a non-blocking close. Any subsequent method
     * invocation on the object will throw an IncorrectState exception.
     */
    public void close() throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Closes this entry. Any subsequent method invocation on the object will
     * throw an IncorrectState exception.
     * 
     * @param timeoutInSeconds
     *            seconds to wait.
     */
    public void close(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     * 
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllow(String id, int permissions, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     * 
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDeny(String id, int permissions, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, IncorrectStateException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Creates a task that obtains the complete URL pointing to the entry.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, URL> getURL(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains a String representing the current working
     * directory for the entry.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, URL> getCWD(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the name part of the URL of this entry.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, URL> getName(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that tests this entry for being a directory.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Boolean> isDir(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that tests this entry for being a namespace entry. If this
     * entry represents a link or a directory, this method returns
     * <code>false</code>, although strictly speaking, directories and links
     * are namespace entries as well.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Boolean> isEntry(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that tests this entry for being a link.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Boolean> isLink(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that returns the URL representing the link target.
     * Resolves one link level only.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, URL> readLink(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that copies this entry to another part of the namespace.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name to copy to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> copy(TaskMode mode, URL target, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that copies this entry to another part of the namespace.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name to copy to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> copy(TaskMode mode, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the target to this
     * entry.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name that will have the symbolic link to this entry.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> link(TaskMode mode, URL target, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the target to this
     * entry.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name that will have the symbolic link to this entry.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> link(TaskMode mode, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that renames this entry to the target, or moves this entry
     * to the target if it is a directory.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name to move to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> move(TaskMode mode, URL target, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that renames this entry to the target, or moves this entry
     * to the target if it is a directory.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            the name to move to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> move(TaskMode mode, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that removes this entry and closes it.
     * 
     * @param mode
     *            the task mode.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> remove(TaskMode mode, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that removes this entry and closes it.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */

    public Task<NSEntry, Void> remove(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that closes this entry. This is a non-blocking close. When
     * the task is done, any subsequent method invocation on the object will
     * throw an IncorrectState exception.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> close(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that closes this entry. When the task is done, any
     * subsequent method invocation on the object will throw an IncorrectState
     * exception.
     * 
     * @param mode
     *            the task mode.
     * @param timeoutInSeconds
     *            seconds to wait.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> close(TaskMode mode, float timeoutInSeconds)
            throws NotImplementedException;

    /**
     * Creates a task that enables the specified permissions for the specified
     * id. An id of "*" enables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> permissionsAllow(TaskMode mode, String id,
            int permissions, int flags) throws NotImplementedException;

    /**
     * Creates a task that disables the specified permissions for the specified
     * id. An id of "*" disables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSEntry, Void> permissionsDeny(TaskMode mode, String id,
            int permissions, int flags) throws NotImplementedException;
}
