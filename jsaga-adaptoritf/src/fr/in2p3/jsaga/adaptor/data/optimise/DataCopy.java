package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataCopy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Do not implement this interface unless it improve efficiency (e.g. third-party transfer)
 */
public interface DataCopy extends DataAdaptor {
    /**
     * Copy this entry to another part of the namespace.
     * @param sourceAbsolutePath the path of the file to be copied.
     * @param targetHost the host of the target server.
     * @param targetPort the post of the target server.
     * @param targetAbsolutePath the path of the file to copy to.
     * @param overwrite if true, then target is overwrited if it exists.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>sourceAbsolutePath</code> is not a file.
     * @throws AlreadyExistsException if <code>targetAbsolutePath</code> already exists and <code>overwrite</code> is false.
     * @throws DoesNotExistException if <code>sourceAbsolutePath</code> does not exist.
     * @throws ParentDoesNotExist if parent of <code>targetAbsolutePath</code> does not exist.
     */
    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs)
        throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException;

    /**
     * Copy this entry to another part of the namespace.
     * @param sourceHost the host of the source server.
     * @param sourcePort the post of the source server.
     * @param sourceAbsolutePath the path of the file to be copied.
     * @param targetAbsolutePath the path of the file to copy to.
     * @param overwrite if true, then target is overwrited if it exists.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>sourceAbsolutePath</code> is not a file.
     * @throws AlreadyExistsException if <code>targetAbsolutePath</code> already exists and <code>overwrite</code> is false.
     * @throws DoesNotExistException if <code>sourceAbsolutePath</code> does not exist.
     */
    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs)
        throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;
}
