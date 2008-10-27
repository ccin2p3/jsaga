package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.ogf.saga.error.*;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataPutterRecursive
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 * TODO: not yet used by the engine...
 */
public interface DataPutterRecursive extends DataWriterAdaptor {
    /**
     * Put content of <code>sourceDir</code> to <code>absolutePath</code>.
     * @param absolutePath the target path.
     * @param additionalArgs adaptor specific arguments.
     * @param sourceDir the source directory.
     * @throws AlreadyExistsException if <code>absolutePath</code> already exists.
     * @throws ParentDoesNotExist if <code>parentAbsolutePath</code> does not exist.
     */
    public void putFromDirectory(String absolutePath, String additionalArgs, File sourceDir)
        throws PermissionDeniedException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;
}
