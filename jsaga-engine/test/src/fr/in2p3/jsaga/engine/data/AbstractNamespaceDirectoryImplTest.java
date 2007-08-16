package fr.in2p3.jsaga.engine.data;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNamespaceDirectoryImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AbstractNamespaceDirectoryImplTest extends TestCase {
    public void test_toRegexp() throws Exception {
        assertNull(AbstractNamespaceDirectoryImpl._toRegexp("*"));
        assertEquals(
                ".*Test\\.java",
                AbstractNamespaceDirectoryImpl._toRegexp("*Test.java").pattern()
        );
    }
}
