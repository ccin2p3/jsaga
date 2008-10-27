package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalReaderMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LogicalReaderMetaData extends LogicalReader {
    /**
     * Lists all the entries in the directory <code>logicalDir</code> with matching meta-data.
     * @param logicalDir absolute path of the logical directory.
     * @param keyValuePatterns map of meta-data keys to values of entries to be found.
     * @param additionalArgs adaptor specific arguments
     * @return attributes of the matching entries.
     * @throws DoesNotExistException if <code>absolutePath</code> does not exist.
     */
    public FileAttributes[] listAttributes(String logicalDir, Map keyValuePatterns, String additionalArgs)
        throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * List the meta data of the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param additionalArgs adaptor specific arguments
     * @return a key-value map containing the meta data
     * @throws NoSuccessException if <code>absolutePath</code> does not exist.
     */
    public Map listMetaData(String logicalEntry, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Get a meta data from the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param name name of the metadata to get.
     * @param additionalArgs adaptor specific arguments
     * @return the meta data value.
     * @throws NoSuccessException if <code>logicalEntry</code> does not exist.
     * @throws DoesNotExistException if the meta data does not exist.
     */
    public String getMetaData(String logicalEntry, String name, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException, DoesNotExistException;
}
