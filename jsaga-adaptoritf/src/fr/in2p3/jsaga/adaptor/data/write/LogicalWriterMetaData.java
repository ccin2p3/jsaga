package fr.in2p3.jsaga.adaptor.data.write;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalWriterMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LogicalWriterMetaData extends LogicalWriter {
    /**
     * Set/add a meta data to the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param name name of the metadata.
     * @param values values of the metadata.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>values</code> contains unsupported characters.
     * @throws NoSuccessException if <code>logicalEntry</code> does not exist.
     */
    public void setMetaData(String logicalEntry, String name, String[] values, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Remove a meta data from the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param name name of the metadata to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws NoSuccessException if <code>logicalEntry</code> does not exist.
     * @throws DoesNotExistException if the meta data does not exist.
     */
    public void removeMetaData(String logicalEntry, String name, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException, DoesNotExistException;
}
