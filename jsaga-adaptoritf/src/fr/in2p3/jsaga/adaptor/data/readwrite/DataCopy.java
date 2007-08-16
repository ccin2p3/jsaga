package fr.in2p3.jsaga.adaptor.data.readwrite;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
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
     * @throws BadParameter if <code>sourceAbsolutePath</code> is not a file.
     * @throws AlreadyExists if the target already exists and <code>overwrite</code> is false.
     * @throws IncorrectState if the source does not exist.
     */
    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess;
}
