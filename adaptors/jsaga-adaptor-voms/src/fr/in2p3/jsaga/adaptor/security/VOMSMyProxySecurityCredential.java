package fr.in2p3.jsaga.adaptor.security;

import org.ietf.jgss.GSSCredential;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxySecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxySecurityCredential extends VOMSSecurityCredential {
    public VOMSMyProxySecurityCredential(GSSCredential proxy, File certRepository) {
        super(proxy, certRepository);
    }
}
