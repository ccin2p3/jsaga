package fr.in2p3.jsaga.adaptor.data.write;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataWriterAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataWriterAdaptor extends DataAdaptor {
    /**
     * Creates a new directory <code>directoryName</code>.
     * @param parentAbsolutePath the parent directory.
     * @param directoryName the directory to create.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>parentAbsolutePath</code> is not a directory.
     * @throws AlreadyExistsException if <code>directoryName</code> already exists.
     * @throws fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist if <code>parentAbsolutePath</code> does not exist.
     */
    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;

    /**
     * Removes the directory <code>absolutePath</code>.
     * @param parentAbsolutePath the parent directory.
     * @param directoryName the directory to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>absolutePath</code> is not a directory.
     * @throws DoesNotExistException if <code>absolutePath</code> does not exist.
     * @throws NoSuccessException if <code>absolutePath</code> has some descendants.
     */
    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Removes the file <code>absolutePath</code>.
     * @param parentAbsolutePath the parent directory.
     * @param fileName the file to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if <code>absolutePath</code> is a directory.
     * @throws DoesNotExistException if <code>absolutePath</code> does not exist.
     */
    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;
}
