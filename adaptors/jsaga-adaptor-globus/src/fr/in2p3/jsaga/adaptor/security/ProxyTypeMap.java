package fr.in2p3.jsaga.adaptor.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ProxyTypeMap
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description:                                      */
public class ProxyTypeMap {

    public static final String TYPE_GLOBUS2 = "OLD";
    public static final String TYPE_GLOBUS3 = "GLOBUS";
    public static final String TYPE_RFC3820 = "RFC3820";
    private static Map<String, Integer> map = createMap();
    
    private static Map<String, Integer> createMap() {  
       Map<String, Integer> map = new HashMap<String, Integer>();
       map.put(TYPE_GLOBUS2, GlobusProxyFactory.OID_OLD);
       map.put(TYPE_GLOBUS3, GlobusProxyFactory.OID_GLOBUS);
       map.put(TYPE_RFC3820, GlobusProxyFactory.OID_RFC3820);
       return java.util.Collections.unmodifiableMap(map);  
    }

    public static int toProxyType(String type) {
        return map.get(type.toUpperCase());
    }
    
    public static boolean isValid(String type) {
        return map.containsKey(type.toUpperCase());
    }
    
    public static Set<String> getValidTypes() {
        return map.keySet();
    }
    
    public static String getExpected() {
        Set<String> valids = ProxyTypeMap.getValidTypes();
        String msg = "\"" + GlobusContext.PROXYTYPE + "\": Expected: ";
        for (Iterator<String> i = valids.iterator(); i.hasNext();) {
            msg += i.next() + " | ";
        }
        return msg;
    }
}
