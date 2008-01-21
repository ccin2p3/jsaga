package fr.in2p3.jsaga.impl.attributes;

import junit.framework.TestCase;
import org.ogf.saga.ObjectType;

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
public class AbstractAttributesImplTest extends TestCase {
    private AbstractAttributesImpl m_attributes;

    public void setUp() {
        m_attributes = new AbstractAttributesImpl(null) {
            public ObjectType getType() {
                return ObjectType.UNKNOWN;
            }
        };
        m_attributes._addReadOnlyAttribute("mykey", "myvalue");
    }

    public void tearDown() {
        m_attributes = null;
    }

    public void test_findAttributesAll() throws Exception {
        String[] keys = m_attributes.findAttributes("");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    public void test_findAttributesWithKey() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    public void test_findAttributes() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*=myv*");
        assertEquals(1, keys.length);
        assertEquals("mykey", keys[0]);
    }

    public void test_dontFindAttributes() throws Exception {
        String[] keys = m_attributes.findAttributes("myk*=myv");
        assertEquals(0, keys.length);
    }
}
