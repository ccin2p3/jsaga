package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataRename
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 août 2007
* ***************************************************
* Description:                                      */
/**
 * Do not implement this interface unless your implementation improves efficiency
 */
public interface DataRename extends DataAdaptor {
    /**
     * Rename entry <code>sourceAbsolutePath</code> to <code>targetAbsolutePath</code>.
     * @param sourceAbsolutePath the entry to rename.
     * @param targetAbsolutePath the new path of the entry.
     * @param overwrite if true, the target is overwrited if it exists.
     * @param additionalArgs adaptor specific arguments
     * @throws AlreadyExists if <code>targetAbsolutePath</code> already exists and <code>overwrite</code> is false.
     * @throws DoesNotExist if <code>sourceAbsolutePath</code> does not exist.
     */
    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs)
        throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess;
}