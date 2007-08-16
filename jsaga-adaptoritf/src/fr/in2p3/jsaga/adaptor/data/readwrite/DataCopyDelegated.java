package fr.in2p3.jsaga.adaptor.data.readwrite;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataCopyDelegated
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Delegate file transfer to a remote server
 */
public interface DataCopyDelegated extends DataAdaptor {
    /**
     * Set the server.
     * @param host host of the server.
     * @param port port of the server.
     */
    public void setTransferManagementServer(String host, int port);

    /**
     * Request a transfer task.
     * @param sourceAbsoluteURI the path of the file to be copied.
     * @param targetAbsoluteURI the URI of the file to copy to.
     * @param overwrite if true, then target is overwrited if it exists.
     * @throws AlreadyExists if the target already exists and <code>overwrite</code> is false.
     * @throws IncorrectState if the source does not exist.
     */
    public void requestTransfer(URI sourceAbsoluteURI, URI targetAbsoluteURI, boolean overwrite) throws IncorrectState, AlreadyExists;

    /**
     * Monitor the requested transfer task.
     * @throws BadParameter if <code>sourceAbsoluteURI</code> is not a file.
     */
    public void monitorTransfer() throws BadParameter;
}
