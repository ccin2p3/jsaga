package fr.in2p3.jsaga.adaptor.security;

import java.util.HashMap;
import java.util.Map;
import eu.emi.security.authn.x509.proxy.ProxyType;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   EMIProxyTypeMap
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description:                                      */
public class EMIProxyTypeMap extends ProxyTypeMap {

    private static Map<Integer, ProxyType> emi_map = createMap();
    
    private static Map<Integer, ProxyType> createMap() {  
       Map<Integer, ProxyType> emi_map = new HashMap<Integer, ProxyType>();
       emi_map.put(map.get(TYPE_GLOBUS2), ProxyType.LEGACY);
       emi_map.put(map.get(TYPE_GLOBUS3), ProxyType.DRAFT_RFC);
       emi_map.put(map.get(TYPE_RFC3820), ProxyType.RFC3820);
       return java.util.Collections.unmodifiableMap(emi_map);  
    }

    public static ProxyType toEMIProxyType(String type) {
        return emi_map.get(map.get(type.toUpperCase()));
    }
    
}
