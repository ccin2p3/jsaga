package fr.in2p3.jsaga.adaptor.data.write;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataWriterTimes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataWriterTimes extends DataWriterAdaptor {
    /**
     * Set the last modification time of the file absolutePath.
     * @param absolutePath the file.
     * @param additionalArgs adaptor specific arguments.
     * @param lastModified the last modification time.
     * @throws DoesNotExistException if absolutePath does not exist.
     */
    public void setLastModified(String absolutePath, String additionalArgs, long lastModified)
        throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
}
