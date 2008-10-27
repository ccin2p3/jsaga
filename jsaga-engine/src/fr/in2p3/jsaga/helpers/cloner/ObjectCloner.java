package fr.in2p3.jsaga.helpers.cloner;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ObjectCloner
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ObjectCloner<K,V> {
    public Map<K,V> cloneMap(Map<K,V> map) throws CloneNotSupportedException {
        Map<K,V> mapClone = new HashMap<K,V>();
        for (Map.Entry<K,V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            mapClone.put(key, value);
        }
        return mapClone;
    }

    public List<V> cloneList(List<V> list) throws CloneNotSupportedException {
        List<V> listClone = new ArrayList<V>();
        for (V value : list) {
            listClone.add(value);
        }
        return listClone;
    }
}
