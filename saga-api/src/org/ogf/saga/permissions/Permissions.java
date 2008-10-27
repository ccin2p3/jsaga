package org.ogf.saga.permissions;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * This interface describes methods to query and set permissions. The generic
 * type <code>T</code> specifies the object type implementing the permissions.
 * The permissions are described in the {@link Permission} enumeration
 * class.
 */
public interface Permissions<T> extends Async {

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     * 
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     */
    public void permissionsAllow(String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     * 
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     */
    public void permissionsDeny(String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Determines if the specified permissions are enabled for the specified id.
     * An id of "*" queries the permissions for all.
     * 
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to query.
     * @return <code>true</code> if the specified permissions are enabled for
     *         the specified id.
     */
    public boolean permissionsCheck(String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Gets the owner id of the entity.
     * 
     * @return the id of the owner.
     */
    public String getOwner() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Gets the group id of the entity.
     * 
     * @return the id of the group.
     */
    public String getGroup() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

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
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Void> permissionsAllow(TaskMode mode, String id,
            int permissions) throws NotImplementedException;

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
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Void> permissionsDeny(TaskMode mode, String id,
            int permissions) throws NotImplementedException;

    /**
     * Creates a task that determines if the specified permissions are enabled
     * for the specified id. An id of "*" queries the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to query.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> permissionsCheck(TaskMode mode, String id,
            int permissions) throws NotImplementedException;

    /**
     * Creates a task that obtains the owner id of the entity.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String> getOwner(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the group id of the entity.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String> getGroup(TaskMode mode)
            throws NotImplementedException;
}
