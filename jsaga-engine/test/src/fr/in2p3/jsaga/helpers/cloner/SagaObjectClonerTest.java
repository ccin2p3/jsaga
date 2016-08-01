package fr.in2p3.jsaga.helpers.cloner;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SagaObjectClonerTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class SagaObjectClonerTest extends Assert {
    @Test
    public void test_cloneMap() throws Exception {
        URL original = URLFactory.createURL("uri://old");
        Map<String, URL> map = new HashMap<String, URL>();
        map.put("myKey", original);
        URL clone = new SagaObjectCloner<String,URL>().cloneMap(map).get("myKey");
        original.setHost("new");

        assertEquals("uri://new", original.getString());
        assertEquals("uri://old", clone.getString());
    }

    @Test
    public void test_cloneList() throws Exception {
        URL original = URLFactory.createURL("uri://old");
        List<URL> list = new ArrayList<URL>();
        list.add(original);
        URL clone = new SagaObjectCloner<String,URL>().cloneList(list).get(0);
        original.setHost("new");

        assertEquals("uri://new", original.getString());
        assertEquals("uri://old", clone.getString());
    }
}
