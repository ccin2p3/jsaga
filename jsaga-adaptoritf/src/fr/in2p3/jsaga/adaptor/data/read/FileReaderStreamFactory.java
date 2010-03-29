package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReaderStreamFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileReaderStreamFactory extends FileReader {
    /**
     * Get an input stream for the file absolutePath.
     * @param absolutePath the file to read from.
     * @param additionalArgs adaptor specific arguments
     * @return an input stream.
     * @throws BadParameterException if absolutePath is not a file.
     * @throws DoesNotExistException if absolutePath does not exist.
     */
    public InputStream getInputStream(String absolutePath, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;
}
