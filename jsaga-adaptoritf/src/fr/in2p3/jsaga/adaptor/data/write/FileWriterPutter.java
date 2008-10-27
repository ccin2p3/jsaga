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
     * Put content of stream to <code>absolutePath</code>.
     * @param absolutePath the path of the file to put.
     * @param append if true, append stream at the end of file.
     * @param additionalArgs adaptor specific arguments
     * @param stream the input stream.
     * @throws BadParameterException if <code>absolutePath</code> is not a file.
     * @throws AlreadyExistsException if <code>absolutePath</code> already exists and <code>append</code> is false.
     * @throws ParentDoesNotExist if <code>parentAbsolutePath</code> does not exist.
     */
    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;
}
