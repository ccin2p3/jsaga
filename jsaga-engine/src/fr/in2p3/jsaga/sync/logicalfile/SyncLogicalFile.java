package fr.in2p3.jsaga.sync.logicalfile;

import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.List;

/**
 * A LogicalFile provides the means to handle the contents of logical files.
 */
public interface SyncLogicalFile extends SyncNSEntry {

    /**
     * Adds a replica location to the replica set. Note: does never throw an
     * <code>AlreadyExists</code> exception!
     *
     * @param name
     *            the location to add.
     */
    public void addLocationSync(URL name) throws NotImplementedException,
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
    public void removeLocationSync(URL name) throws NotImplementedException,
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
    public void updateLocationSync(URL nameOld, URL nameNew)
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
    public List<URL> listLocationsSync() throws NotImplementedException,
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
    public void replicateSync(URL name, int flags) throws NotImplementedException,
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
    public void replicateSync(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;
}
