package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataFinder
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public interface DataFinder {
    /**
     * Find entries matching pattern <code>namePattern</code>.
     * @param absolutePath the directory containing entries to list.
     * @param namePattern the pattern matching entries names to find.
     * @param additionalArgs adaptor specific arguments
     * @param recursive search recursively if true
     * @return the entry attributes.
     * @throws org.ogf.saga.error.DoesNotExistException if absolutePath does not exist.
     */
    public FileAttributes[] find(String absolutePath, String namePattern, String additionalArgs, boolean recursive)
        throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
}
