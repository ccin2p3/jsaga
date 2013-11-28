package fr.in2p3.jsaga.adaptor.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.emi.security.authn.x509.proxy.ProxyType;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DelegationTypeMap
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description:                                      */
// TODO: simplify this class ... is not really a map ...
public class DelegationTypeMap {

    public static final String LIMITED = "LIMITED";
    public static final String FULL = "FULL";
    public static final String NONE = "NONE";
    private static List<String> map = createList();
    
    private static List<String> createList() {  
       List<String> map = new ArrayList<String>();
       map.add(LIMITED);
       map.add(FULL);
       map.add(NONE);
       return map;
    }

    public static boolean toLimitedValue(String type) {
        return (type.equalsIgnoreCase(LIMITED));
    }
    
    public static boolean isValid(String type) {
        return map.contains(type.toUpperCase());
    }
    
    public static Set<String> getValidTypes() {
        return new HashSet<String>(map);
    }
    
    public static String getExpected() {
        Set<String> valids = DelegationTypeMap.getValidTypes();
        String msg = "\"" + VOMSContext.DELEGATION + "\": Expected: ";
        for (Iterator<String> i = valids.iterator(); i.hasNext();) {
            msg += i.next() + " | ";
        }
        return msg;
    }
}
