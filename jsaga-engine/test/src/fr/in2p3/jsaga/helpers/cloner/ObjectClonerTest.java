package fr.in2p3.jsaga.helpers.cloner;

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
 * File:   ObjectClonerTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class ObjectClonerTest extends Assert {
    @Test
    public void test_shallowCloneMap() throws Exception {
        Map<String, String> original = new HashMap<String, String>();
        original.put("foo", "bar");
        Map<String, String> clone = new ObjectCloner<String, String>().shallowCloneMap(original);
        original.put("foo2", "bar2");

        assertEquals(2, original.size());
        assertEquals(1, clone.size());
    }

    @Test
    public void test_shallowCloneList() throws Exception {
        List<String> original = new ArrayList<String>();
        original.add("foo");
        List<String> clone = new ObjectCloner<String, String>().shallowCloneList(original);
        original.add("foo2");

        assertEquals(2, original.size());
        assertEquals(1, clone.size());
    }
}
