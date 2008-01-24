package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.*;

import java.io.InputStream;

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
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public long getSize(String absolutePath)
        throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess;

    /**
     * Get an input stream for the file <code>absolutePath</code>.
     * @param absolutePath the file to read from.
     * @param additionalArgs adaptor specific arguments
     * @return an input stream.
     * @throws BadParameter if <code>absolutePath</code> is not a file.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public InputStream getInputStream(String absolutePath, String additionalArgs)
        throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess;
}
