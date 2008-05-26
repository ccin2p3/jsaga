package fr.in2p3.jsaga.impl.job.instance.stream;

import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Stdout
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class Stdout extends InputStream {
    public abstract void closeJobIOHandler() throws PermissionDenied, Timeout, NoSuccess;
}
