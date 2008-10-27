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
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/**
 * A LogicalFile provides the means to handle the contents of logical files.
 */
public interface LogicalFile extends NSEntry, AsyncAttributes<LogicalFile> {

    /**
     * Adds a replica location to the replica set. Note: does never throw an
     * <code>AlreadyExists</code> exception!
     * 
     * @param name
     *            the location to add.
     */
    public void addLocation(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Removes a replica location from the replica set.
     * 
     * @param name
     *            the location to remove.
     */
    public void removeLocation(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Changes a replica location in the replica set.
     * 
     * @param nameOld
     *            the location to be updated.
     * @param nameNew
     *            the updated location.
     */
    public void updateLocation(URL nameOld, URL nameNew)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Lists the locations in this location set.
     * 
     * @return the location list.
     */
    public List<URL> listLocations() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Replicates a file from any of the known locations to a new location.
     * 
     * @param name
     *            location to replicate to.
     * @param flags
     *            flags defining the operation modus.
     */
    public void replicate(URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Replicates a file from any of the known locations to a new location, with
     * default flags NONE.
     * 
     * @param name
     *            location to replicate to.
     */
    public void replicate(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Creates a task that adds a replica location to the replica set.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the location to add.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, Void> addLocation(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that removes a replica location from the replica set.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the location to remove.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, Void> removeLocation(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that changes a replica location in the replica set.
     * 
     * @param mode
     *            the task mode.
     * @param nameOld
     *            the location to be updated.
     * @param nameNew
     *            the updated location.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, Void> updateLocation(TaskMode mode, URL nameOld,
            URL nameNew) throws NotImplementedException;

    /**
     * Creates a task that lists the locations in this location set.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, List<URL>> listLocations(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that replicates a file from any of the known locations to
     * a new location.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location to replicate to.
     * @param flags
     *            flags defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, Void> replicate(TaskMode mode, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that replicates a file from any of the known locations to
     * a new location, with default flags NONE.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location to replicate to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<LogicalFile, Void> replicate(TaskMode mode, URL name)
            throws NotImplementedException;
}
