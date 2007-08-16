package fr.in2p3.jsaga.adaptor.data.write;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataWriterAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataWriterAdaptor extends DataAdaptor {
    /**
     * Removes the entry <code>absolutePath</code>.
     * @param absolutePath the absolute path of the entry.
     * @throws BadParameter if <code>absolutePath</code> is a directory.
     * @throws IncorrectState if <code>absolutePath</code> does not exist.
     */
    public void removeEntry(String absolutePath)
        throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess;
}
