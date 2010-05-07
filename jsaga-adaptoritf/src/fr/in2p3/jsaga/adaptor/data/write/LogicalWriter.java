package fr.in2p3.jsaga.adaptor.data.write;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalWriter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LogicalWriter extends DataWriterAdaptor {
    /**
     * Add a replica location to the replica set.
     * @param logicalEntry absolute path of the logical entry.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if logicalEntry is a directory.
     * @throws AlreadyExistsException if logicalEntry already exists.
     * @throws ParentDoesNotExist if parent directory of logicalEntry does not exist.
     */
    public void create(String logicalEntry, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;

    /**
     * Add a replica location to the replica set.
     * @param logicalEntry absolute path of the logical entry.
     * @param replicaEntry location to add to set.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if logicalEntry is a directory.
     * @throws IncorrectStateException if logicalEntry does not exist.
     */
    public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Remove a replica location from the replica set.
     * @param logicalEntry absolute path of the logical entry.
     * @param replicaEntry replica to remove from set.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if logicalEntry is a directory.
     * @throws IncorrectStateException if logicalEntry does not exist.
     * @throws DoesNotExistException if the replicaEntry is not in the set of replicas.
     */
    public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException;
}
