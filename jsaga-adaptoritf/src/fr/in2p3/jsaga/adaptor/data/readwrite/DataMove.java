package fr.in2p3.jsaga.adaptor.data.readwrite;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataMove
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Do not implement this interface unless it improve efficiency (e.g. renaming a local file)
 */
public interface DataMove extends DataAdaptor {
    /**
     * Renames this entry to the target, or moves this entry to the target
     * if it is a directory.
     * @param sourceAbsolutePath the file to be moved.
     * @param targetHost the host of the target server.
     * @param targetPort the post of the target server.
     * @param targetAbsolutePath the path to move to.
     * @param overwrite if true, the target is overwrited if it exists.
     * @throws BadParameter if <code>sourceAbsolutePath</code> is not a file.
     * @throws AlreadyExists if the target already exists and <code>overwrite</code> is false.
     * @throws IncorrectState if the source does not exist.
     */
    public void move(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess;
}
