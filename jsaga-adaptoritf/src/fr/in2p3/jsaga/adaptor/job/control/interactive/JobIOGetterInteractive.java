package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOGetterInteractive
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOGetterInteractive extends JobIOGetter {
    /**
     * @return the job standard input stream
     */
    public OutputStream getStdin() throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
