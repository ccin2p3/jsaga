package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StreamCallback
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 avr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface StreamCallback {
    public void freeInputStream(String token, String srmPath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
    public void freeOutputStream(String token, String srmPath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
}
