package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import org.ogf.saga.error.*;

import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Stdin
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class Stdin extends OutputStream {
    public abstract void openJobIOHandler(JobIOGetterInteractive ioHandler) throws NotImplementedException, PermissionDeniedException, TimeoutException, NoSuccessException;
    public abstract byte[] getBuffer();
}
