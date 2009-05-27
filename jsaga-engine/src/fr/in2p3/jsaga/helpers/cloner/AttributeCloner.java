package fr.in2p3.jsaga.helpers.cloner;

import fr.in2p3.jsaga.impl.attributes.Attribute;

import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AttributeCloner
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   27 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class AttributeCloner<K> {
    public Map<K,Attribute> cloneMap(Map<K,Attribute> map) throws CloneNotSupportedException {
        Map<K,Attribute> mapClone = new HashMap<K,Attribute>();
        for (Map.Entry<K,Attribute> entry : map.entrySet()) {
            K key = entry.getKey();
            Attribute value = entry.getValue();
            Attribute valueClone = value.clone();
            mapClone.put(key, valueClone);
        }
        return mapClone;
    }

    public List<Attribute> cloneList(List<Attribute> list) throws CloneNotSupportedException {
        List<Attribute> listClone = new ArrayList<Attribute>();
        for (Attribute value : list) {
            Attribute valueClone = value.clone();
            listClone.add(valueClone);
        }
        return listClone;
    }
}
