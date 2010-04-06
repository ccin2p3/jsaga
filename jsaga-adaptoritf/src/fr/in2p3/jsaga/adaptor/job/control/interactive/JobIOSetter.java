package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOSetter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOSetter extends JobIOHandler {
    /**
     * Set the job standard output stream.
     * @param out the job standard output stream
     */
    public void setStdout(OutputStream out) throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Set the job standard error stream.
     * @param err the job standard error stream
     */
    public void setStderr(OutputStream err) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
