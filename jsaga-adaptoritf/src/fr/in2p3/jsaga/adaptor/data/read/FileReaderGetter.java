package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.*;

import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReaderGetter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileReaderGetter extends FileReader {
    /**
     * Get content of absolutePath to stream.
     * @param absolutePath the path of the file to get.
     * @param stream the output stream.
     * @param additionalArgs adaptor specific arguments
     * @throws BadParameterException if absolutePath is not a file.
     * @throws DoesNotExistException if absolutePath does not exist.
     */
    public void getToStream(String absolutePath, String additionalArgs, OutputStream stream)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;
}
