package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.AbstractDummyDataAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DummyGSIFTPAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DummyGSIFTPAdaptor extends AbstractDummyDataAdaptor {
    public String getType() {
        return "gsiftp";
    }
}
