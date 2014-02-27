package fr.in2p3.jsaga.impl.attributes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAttributesImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AbstractAttributesImplTest extends Assert {
    private AbstractAttributesImpl m_attributes;

    @Before
    public void setUp() {
        m_attributes = new AbstractAttributesImpl(null) {};
        m_attributes._addReadOnlyAttribute("mykey", "myvalue");
    }

    @After
    public void tearDown() {
        m_attributes = null;
    }

    @Test
    public void test_findAttributesAll() throws Exception {
        String[] keys = m_attributes.findAttributes("");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    @Test
    public void test_findAttributesWithKey() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    @Test
    public void test_findAttributes() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*=myv*");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    @Test
    public void test_dontFindAttributes() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*=myv");
        assertEquals(0, keys.length);
    }
}
