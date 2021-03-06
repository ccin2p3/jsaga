package fr.in2p3.jsaga.adaptor.data.write;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriterPutter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileWriterPutter extends FileWriter {
    /**
     * Put content of stream to absolutePath.
     * @param absolutePath the path of the file to put.
     * @param append if true, append stream at the end of file.
     * @param additionalArgs adaptor specific arguments
     * @param stream the input stream.
     * @throws BadParameterException if absolutePath is not a file.
     * @throws AlreadyExistsException if absolutePath already exists and append is false.
     * @throws ParentDoesNotExist if parentAbsolutePath does not exist.
     */
    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;
    
    /**
     * Defines the buffer size in bytes for the PipedInputStream pipe
     * @see java.io.PipedInputStream
     * @return the pipe size used for the PipedInputStream (return 0 if you do not know what all this means)
     */
    public int getBufferSize();
}
