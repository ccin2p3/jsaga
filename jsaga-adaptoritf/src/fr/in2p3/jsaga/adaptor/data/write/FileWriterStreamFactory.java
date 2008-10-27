package fr.in2p3.jsaga.adaptor.data.write;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import org.ogf.saga.error.*;

import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriterStreamFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileWriterStreamFactory extends FileWriter {
    /**
     * Get an output stream for the file <code>fileName</code>.
     * @param parentAbsolutePath the parent directory.
     * @param fileName the file to write to.
     * @param exclusive if true, throw exception if file already exist.
     * @param append if true, append stream at the end of file.
     * @param additionalArgs adaptor specific arguments
     * @return an output stream.
     * @throws BadParameterException if <code>parentAbsolutePath</code> is not a directory.
     * @throws AlreadyExistsException if <code>fileName</code> already exists and <code>exclusive</code> and <code>append</code> are both false.
     * @throws ParentDoesNotExist if <code>parentAbsolutePath</code> does not exist.
     */
    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs)
        throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException;
}
