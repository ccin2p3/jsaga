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
     * Creates a new directory directoryName.
     * @param parentAbsolutePath the parent directory.
     * @param directoryName the directory to create.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if parentAbsolutePath is not a directory.
     * @throws AlreadyExistsException if directoryName already exists.
     * @throws fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist if parentAbsolutePath does not exist.
     */
    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;

    /**
     * Removes the directory absolutePath.
     * @param parentAbsolutePath the parent directory.
     * @param directoryName the directory to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if absolutePath is not a directory.
     * @throws DoesNotExistException if absolutePath does not exist.
     * @throws NoSuccessException if absolutePath has some descendants.
     */
    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Removes the file absolutePath.
     * @param parentAbsolutePath the parent directory.
     * @param fileName the file to remove.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if absolutePath is a directory.
     * @throws DoesNotExistException if absolutePath does not exist.
     */
    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;
}
