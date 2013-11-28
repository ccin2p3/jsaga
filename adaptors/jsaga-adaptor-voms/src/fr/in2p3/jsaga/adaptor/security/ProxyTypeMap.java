package fr.in2p3.jsaga.adaptor.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.emi.security.authn.x509.proxy.ProxyType;

public class ProxyTypeMap {

    public static final String TYPE_GLOBUS2 = "OLD";
    public static final String TYPE_GLOBUS3 = "GLOBUS";
    public static final String TYPE_RFC3820 = "RFC3820";
    private static Map<String, ProxyType> map = createMap();
    
    private static Map<String, ProxyType> createMap() {  
       Map<String, ProxyType> map = new HashMap<String, ProxyType>();
       map.put(TYPE_GLOBUS2, ProxyType.LEGACY);
       map.put(TYPE_GLOBUS3, ProxyType.DRAFT_RFC);
       map.put(TYPE_RFC3820, ProxyType.RFC3820);
       return java.util.Collections.unmodifiableMap(map);  
    }

    public static ProxyType toProxyType(String type) {
        return map.get(type.toUpperCase());
    }
    
    public static boolean isValid(String type) {
        return map.containsKey(type.toUpperCase());
    }
    
    public static Set<String> getValidTypes() {
        return map.keySet();
    }
}
