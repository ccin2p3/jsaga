package org.ogf.saga.permissions;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * This interface describes methods to query and set permissions.
 */
public interface Permissions extends Async {

    // In the SAGA API specification, there is one method: set_permission,
    // with a boolean argument "allow".

    /**
     * Enables the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param id the id.
     * @param permissions the permissions to enable.
     */
    public void enablePermissions(String id, Permission... permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;

    /**
     * Disables the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param id the id.
     * @param permissions the permissions to disable.
     */
    public void disablePermissions(String id, Permission... permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;

    /**
     * Determines if the specified permissions are enabled for the
     * specified id.
     * An id of "*" queries the permissions for all.
     * @param id the id.
     * @param permissions the permissions to query.
     * @return <code>true</code> if the specified permissions are enabled
     *     for the specified id.
     */
    public boolean areEnabled(String id, Permission... permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, Timeout, NoSuccess;

    /**
     * Gets the owner id of the entity.
     * @return the id of the owner.
     */
    public String getOwner()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, Timeout, NoSuccess;

    /**
     * Gets the group id of the entity.
     * @return the id of the group.
     */
    public String getGroup()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, Timeout, NoSuccess;

    /**
     * Creates a task that enables the specified permissions for the
     * specified id.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task enablePermissions(TaskMode mode, String id,
            Permission... permissions)
        throws NotImplemented;

    /**
     * Creates a task that disables the specified permissions for the
     * specified id.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task disablePermissions(TaskMode mode, String id,
            Permission... permissions)
        throws NotImplemented;

    /**
     * Creates a task that determines if the specified permissions are
     * enabled for the specified id.
     * An id of "*" queries the permissions for all.
     * @param mode determines the initial state of the task.
     * @param id the id.
     * @param permissions the permissions to query.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> areEnabled(TaskMode mode, String id,
            Permission... permissions)
        throws NotImplemented;

    /**
     * Creates a task that obtains the owner id of the entity.
     * @param mode determines the initial state of the task.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String> getOwner(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that obtains the group id of the entity.
     * @param mode determines the initial state of the task.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String> getGroup(TaskMode mode)
        throws NotImplemented;
}
