package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOGetter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOGetter extends JobIOHandler {
    public OutputStream getStdin() throws PermissionDenied, Timeout, NoSuccess;

    public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess;

    public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess;
}
