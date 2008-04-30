package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOSetter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOSetter extends JobIOSetterPseudo {
    public void setStdin(InputStream in) throws PermissionDenied, Timeout, NoSuccess;
}
