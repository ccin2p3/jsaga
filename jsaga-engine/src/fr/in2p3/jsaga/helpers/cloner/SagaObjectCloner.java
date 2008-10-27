package fr.in2p3.jsaga.helpers.cloner;

import org.ogf.saga.SagaObject;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaObjectCloner
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SagaObjectCloner<K,V extends SagaObject> {
    public Map<K,V> cloneMap(Map<K,V> map) throws CloneNotSupportedException {
        Map<K,V> mapClone = new HashMap<K,V>();
        for (Map.Entry<K,V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            V valueClone = (V) value.clone();
            mapClone.put(key, valueClone);
        }
        return mapClone;
    }

    public List<V> cloneList(List<V> list) throws CloneNotSupportedException {
        List<V> listClone = new ArrayList<V>();
        for (V value : list) {
            V valueClone = (V) value.clone();
            listClone.add(valueClone);
        }
        return listClone;
    }
}
