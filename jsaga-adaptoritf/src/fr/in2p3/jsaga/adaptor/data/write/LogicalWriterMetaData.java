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
     * @param value value of the metadata.
     * @param additionalArgs adaptor specific arguments
     * @throws NoSuccess if <code>logicalEntry</code> does not exist.
     */
    public void setMetaData(String logicalEntry, String name, String value, String additionalArgs)
        throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Remove a meta data from the logical entry.
     * @param logicalEntry absolute path of the logical entry.
     * @param name name of the metadata to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws NoSuccess if <code>logicalEntry</code> does not exist.
     * @throws DoesNotExist if the meta data does not exist.
     */
    public void removeMetaData(String logicalEntry, String name, String additionalArgs)
        throws PermissionDenied, Timeout, NoSuccess, DoesNotExist;
}
