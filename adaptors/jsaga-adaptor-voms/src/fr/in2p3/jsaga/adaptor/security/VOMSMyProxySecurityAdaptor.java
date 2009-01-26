package fr.in2p3.jsaga.adaptor.security;

import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxySecurityAdaptor extends VOMSSecurityAdaptor {
    public VOMSMyProxySecurityAdaptor(GSSCredential proxy) {
        super(proxy);
    }
}
