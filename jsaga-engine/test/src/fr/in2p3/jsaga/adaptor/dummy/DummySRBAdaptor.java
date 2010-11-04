package fr.in2p3.jsaga.adaptor.dummy;

import fr.in2p3.jsaga.adaptor.dummy.abstracts.AbstractDummyDataAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DummySRBAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DummySRBAdaptor extends AbstractDummyDataAdaptor {
    public String getType() {
        return "srb";
    }
}
