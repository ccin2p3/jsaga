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
     * List the meta data of the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param additionalArgs adaptor specific arguments
     * @return a String to String[] map containing the meta data
     * @throws NoSuccessException if <code>absolutePath</code> does not exist.
     */
    public Map listMetaData(String logicalEntry, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Lists all the entries in the directory <code>logicalDir</code> with matching meta-data.
     * @param logicalDir absolute path of the logical directory.
     * @param keyValuePatterns map of meta-data keys to values of entries to be found.
     *        Patterns are case-sensitive. Accepted wild-cards are '*' and '?'.
     * @param recursive tell if search must be recursive or not.
     * @param additionalArgs adaptor specific arguments.
     * @return attributes of the matching entries.
     * @throws DoesNotExistException if <code>absolutePath</code> does not exist.
     */
    public FileAttributes[] findAttributes(String logicalDir, Map keyValuePatterns, boolean recursive, String additionalArgs)
        throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
}
