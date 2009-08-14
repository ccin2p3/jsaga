package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.optimise.expr.BooleanExpr;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
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
     * Lists entries in the directory <code>logicalDir</code>, filtered with <code>filter</code>.
     * @param logicalDir absolute path of the logical directory.
     * @param namePattern a pattern matching the name of the entries.
     * @param filter boolean expression for filtering with meta-data.
     * @param recursive tell if search must be recursive or not.
     * @param additionalArgs adaptor specific arguments.
     * @return the entry attributes.
     * @throws BadParameterException if <code>logicalDir</code> is not a directory.
     * @throws DoesNotExistException if <code>logicalDir</code> does not exist.
     */
    public FileAttributes[] findAttributes(String logicalDir, String namePattern, BooleanExpr filter, boolean recursive, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Recursively list the names of the metadata from <code>baseLogicalDir</code>.
     * @param baseLogicalDir absolute path to the base directory.
     * @param additionalArgs adaptor specific arguments
     * @return a list of metadata names
     * @throws NoSuccessException if <code>baseLogicalDir</code> does not exist.
     */
    public String[] listMetadataNames(String baseLogicalDir, String additionalArgs)
        throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
