package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOGetter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOGetter extends JobIOHandler {
    public InputStream getStdout() throws PermissionDeniedException, TimeoutException, NoSuccessException;

    public InputStream getStderr() throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
