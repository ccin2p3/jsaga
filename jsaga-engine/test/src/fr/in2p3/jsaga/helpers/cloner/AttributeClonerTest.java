package fr.in2p3.jsaga.helpers.cloner;

import fr.in2p3.jsaga.impl.attributes.Attribute;
import fr.in2p3.jsaga.impl.attributes.AttributeScalar;
import fr.in2p3.jsaga.impl.attributes.DefaultAttributeScalar;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AttributeClonerTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class AttributeClonerTest extends Assert {
    @Test
    public void test_cloneMap() throws Exception {
        AttributeScalar original = new DefaultAttributeScalar("", "oldValue");
        Map<String, Attribute> map = new HashMap<String, Attribute>();
        map.put("myKey", original);
        AttributeScalar clone = (AttributeScalar) new AttributeCloner<String>().cloneMap(map).get("myKey");
        original.setValue("newValue");

        assertEquals("newValue", original.getValue());
        assertEquals("oldValue", clone.getValue());
    }

    @Test
    public void test_cloneList() throws Exception {
        AttributeScalar original = new DefaultAttributeScalar("", "oldValue");
        List<Attribute> list = new ArrayList<Attribute>();
        list.add(original);
        AttributeScalar clone = (AttributeScalar) new AttributeCloner<String>().cloneList(list).get(0);
        original.setValue("newValue");

        assertEquals("newValue", original.getValue());
        assertEquals("oldValue", clone.getValue());
    }
}
