package org.ogf.saga.namespace;

import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * Defines methods that allow inspection and management of the entry.
 */
public interface NSEntry extends SagaObject, Async, Permissions {

    /**
     * Obtains the complete URL refering to the entry.
     * @return the URL.
     */
    public URL getURL()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Obtains the current working directory for the entry.
     * @return the current working directory.
     */
    public String getCWD()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Obtains the name part of the URL of this entry.
     * @return the name part.
     */
    public String getName()
        throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * Tests this entry for being a directory.
     * @return true if the entry is a directory.
     */
    public boolean isDir()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Tests this entry for being a namespace entry. If this entry represents
     * a link or a directory, this method returns <code>false</code>, although
     * strictly speaking, directories and links are namespace entries as well.
     * @return true if the entry is a namespace entry.
     */
    public boolean isEntry()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Tests this entry for being a link.
     * @return true if the entry is a link.
     */
    public boolean isLink()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;
    
    /**
     * Returns the URL representing the link target. Resolves one link level
     * only.
     * @return the link target.
     */
    public URL readLink()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Copies this entry to another part of the namespace.
     * @param target the name to copy to.
     * @param flags defining the operation modus.
     */
    public void copy(URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, AlreadyExists,
            Timeout, NoSuccess, IncorrectURL;

    /**
     * Creates a symbolic link from the target to this entry.
     * @param target the name that will have the symbolic link to this entry.
     * @param flags defining the operation modus.
     */
    public void link(URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, AlreadyExists,
            Timeout, NoSuccess, IncorrectURL;

    /**
     * Renames this entry to the target, or moves this entry to the target
     * if it is a directory.
     * @param target the name to move to.
     * @param flags defining the operation modus.
     */
    public void move(URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, AlreadyExists,
            Timeout, NoSuccess, IncorrectURL;


    /**
     * Removes this entry and closes it.
     * @param flags defining the operation modus.
     */
    public void remove(int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Closes this entry.
     * This is a non-blocking close. Any subsequent method invocation on the
     * object will throw an IncorrectState exception.
     */
    public void close()
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Closes this entry.
     * Any subsequent method invocation on the
     * object will throw an IncorrectState exception.
     * @param timeoutInSeconds seconds to wait.
     */
    public void close(float timeoutInSeconds)
        throws NotImplemented, IncorrectState, NoSuccess;

    /**
     * Allows the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllow(String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess;

    /**
     * Denies the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDeny(String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;

    //
    // Task versions ...
    //

    /**
     * Creates a task that obtains the complete URL pointing to the entry.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<URL> getURL(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains an URL representing the current working
     * directory for the entry.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<URL> getCWD(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the name part of the URL of this entry.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<String> getName(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that tests this entry for being a directory.
     * @param mode the task mode.
     * @param flags flags for the operation. The only allowed flag is
     *     DEREFERENCE. Other flags cause a BadParameter exception.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isDir(TaskMode mode, int flags)
        throws NotImplemented;

    /**
     * Creates a task that tests this entry for being a namespace entry.
     * If this entry represents
     * a link or a directory, this method returns <code>false</code>, although
     * strictly speaking, directories and links are namespace entries as well.
     * @param mode the task mode.
     * @param flags flags for the operation. The only allowed flag is
     *     DEREFERENCE. Other flags cause a BadParameter exception.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isEntry(TaskMode mode, int flags)
        throws NotImplemented;

    /**
     * Creates a task that tests this entry for being a link.
     * @param mode the task mode.
     * @param flags flags for the operation. The only allowed flag is
     *     DEREFERENCE. Other flags cause a BadParameter exception.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isLink(TaskMode mode, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that returns the URL representing the link target.
     * Resolves one link level only.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<URL> readLink(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that copies this entry to another part of the namespace.
     * @param mode the task mode.
     * @param target the name to copy to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task copy(TaskMode mode, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a symbolic link from the target to this
     * entry.
     * @param mode the task mode.
     * @param target the name that will have the symbolic link to this entry.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task link(TaskMode mode, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that renames this entry to the target, or moves this
     * entry to the target if it is a directory.
     * @param mode the task mode.
     * @param target the name to move to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task move(TaskMode mode, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that removes this entry and closes it.
     * @param mode the task mode.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task remove(TaskMode mode, int flags)
        throws NotImplemented;

    /**
     * Creates a task that closes this entry.
     * This is a non-blocking close. When the task is done,
     * any subsequent method invocation on the
     * object will throw an IncorrectState exception.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that closes this entry.
     * When the task is done, any subsequent method invocation on the
     * object will throw an IncorrectState exception.
     * @param mode the task mode.
     * @param timeoutInSeconds seconds to wait.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task close(TaskMode mode, float timeoutInSeconds)
        throws NotImplemented;

    /**
     * Creates a task that enables the specified permissions for the
     * specified id.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsAllow(TaskMode mode, String id,
            int permissions, int flags)
        throws NotImplemented;

    /**
     * Creates a task that disables the specified permissions for the
     * specified id.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsDeny(TaskMode mode, String id,
            int permissions, int flags)
        throws NotImplemented;
}
