package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalReaderMetaDataExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LogicalReaderMetaDataExtended extends LogicalReaderMetaData {
    /**
     * Recursively list the names of the metadata from baseLogicalDir.
     * @param baseLogicalDir absolute path to the base directory.
     * @param additionalArgs adaptor specific arguments
     * @return a list of metadata names
     * @throws NoSuccessException if baseLogicalDir does not exist.
     */
    public String[] listMetadataNames(String baseLogicalDir, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Recursively list the values of metadata <code>key</code> from baseLogicalDir.
     * @param baseLogicalDir absolute path to the base directory.
     * @param key the metadata key.
     * @param additionalArgs adaptor specific arguments
     * @return a list of metadata values
     * @throws NoSuccessException if baseLogicalDir does not exist.
     */
    public String[] listMetadataValues(String baseLogicalDir, String key, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
