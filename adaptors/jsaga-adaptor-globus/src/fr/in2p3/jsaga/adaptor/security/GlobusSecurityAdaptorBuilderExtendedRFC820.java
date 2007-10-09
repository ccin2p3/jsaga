package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorBuilderExtendedRFC820
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorBuilderExtendedRFC820 extends GlobusSecurityAdaptorBuilderExtendedAbstract {
    public String getType() {
        return "GlobusRFC820";
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        new GlobusProxyFactory(attributes, GlobusProxyFactory.OID_RFC820).createProxy();
    }
}
