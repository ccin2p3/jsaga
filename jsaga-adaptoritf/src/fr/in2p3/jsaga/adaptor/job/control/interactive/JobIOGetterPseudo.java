package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOGetterPseudo
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOGetterPseudo extends JobIOHandler {
    public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess;

    public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess;
}
