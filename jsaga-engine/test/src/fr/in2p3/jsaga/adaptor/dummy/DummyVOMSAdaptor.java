package fr.in2p3.jsaga.adaptor.dummy;

import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.dummy.abstracts.AbstractDummySecurityAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DummyVOMSAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DummyVOMSAdaptor extends AbstractDummySecurityAdaptor {
    public String getType() {
        return "VOMS";
    }
    
    public Usage getUsage() {
        return new UAnd.Builder()
            .and(new UOptional("Att"))
            .and(new UOptional("UserProxy"))
            .build();
    }

}
