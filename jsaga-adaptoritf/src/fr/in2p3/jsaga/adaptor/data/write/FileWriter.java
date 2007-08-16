package fr.in2p3.jsaga.adaptor.data.write;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileWriter extends DataWriterAdaptor {
    /**
     * Get a stream writer for the file <code>absolutePath</code>.
     * @param absolutePath the file to write to.
     * @param append if true, append stream at the end of file.
     * @return a stream writer.
     * @throws AlreadyExists if <code>absolutePath</code> already exists and <code>overwrite</code> and <code>append</code> are both false.
     * @throws DoesNotExist if <code>absolutePath</code> has no parent directory.
     */
    public FileWriterStream openFileWriterStream(String absolutePath, boolean overwrite, boolean append)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, AlreadyExists, DoesNotExist, Timeout, NoSuccess;
}
