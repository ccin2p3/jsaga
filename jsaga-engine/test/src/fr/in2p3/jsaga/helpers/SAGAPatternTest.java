package fr.in2p3.jsaga.helpers;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGAPatternTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGAPatternTest extends TestCase {
    public void test_toRegexp() throws Exception {
        assertNull(SAGAPattern.toRegexp("*"));
        assertEquals(
                ".*Test\\.java",
                SAGAPattern.toRegexp("*Test.java").pattern()
        );
    }
}
