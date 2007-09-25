package org.ogf.saga.logicalfile;

import java.util.List;

import org.ogf.saga.URI;
import org.ogf.saga.attributes.Attributes;
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
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceEntry;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * A LogicalFile provides the means to handle the contents of logical
 * files.
 */
public interface LogicalFile extends NamespaceEntry, Attributes {

    /**
     * Adds a replica location to the replica set.
     * Note: does never throw an <code>AlreadyExists</code> exception!
     * @param name the location to add.
     */
    public void addLocation(URI name)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Removes a replica location from the replica set.
     * @param name the location to remove.
     */
    public void removeLocation(URI name)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Changes a replica location in the replica set.
     * @param nameOld the location to be updated.
     * @param nameNew the updated location.
     */
    public void updateLocation(URI nameOld, URI nameNew)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Lists the locations in this location set.
     * @return the location list.
     */
    public List<URI> listLocations()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Replicates a file from any of the known locations to a new location.
     * @param name location to replicate to.
     * @param flags flags defining the operation modus.
     */
    public void replicate(URI name, Flags... flags)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    //
    // Task versions ...
    //

    /**
     * Creates a task that adds a replica location to the replica set.
     * @param mode the task mode.
     * @param name the location to add.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task addLocation(TaskMode mode, URI name)
        throws NotImplemented;

    /**
     * Creates a task that removes a replica location from the replica set.
     * @param mode the task mode.
     * @param name the location to remove.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task removeLocation(TaskMode mode, URI name)
        throws NotImplemented;

    /**
     * Creates a task that changes a replica location in the replica set.
     * @param mode the task mode.
     * @param nameOld the location to be updated.
     * @param nameNew the updated location.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task updateLocation(TaskMode mode, URI nameOld, URI nameNew)
        throws NotImplemented;

    /**
     * Creates a task that lists the locations in this location set.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<List<URI>> listLocations(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that replicates a file from any of the known locations
     * to a new location.
     * @param mode the task mode.
     * @param name location to replicate to.
     * @param flags flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task replicate(TaskMode mode, URI name, Flags... flags)
        throws NotImplemented;
}
