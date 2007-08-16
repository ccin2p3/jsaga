package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReader
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileReader extends DataReaderAdaptor {
    /**
     * Get the number of bytes in the file <code>absolutePath</code>.
     * @param absolutePath the file.
     * @return the number of bytes.
     * @throws BadParameter if <code>absolutePath</code> is not a file.
     * @throws IncorrectState if <code>absolutePath</code> does not exist.
     */
    public int getSize(String absolutePath)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Get a stream reader for the file <code>absolutePath</code>.
     * @param absolutePath the file to read from.
     * @return a stream reader.
     * @throws BadParameter if <code>absolutePath</code> is not a file.
     * @throws IncorrectState if <code>absolutePath</code> does not exist.
     */
    public FileReaderStream openFileReaderStream(String absolutePath)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;
}
