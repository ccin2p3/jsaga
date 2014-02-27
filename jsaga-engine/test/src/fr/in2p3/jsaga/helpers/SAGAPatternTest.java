package fr.in2p3.jsaga.helpers;

import org.junit.Assert;
import org.junit.Test;

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
public class SAGAPatternTest extends Assert {
    @Test
    public void test_toRegexp() throws Exception {
        assertNull(SAGAPattern.toRegexp("*"));
        assertEquals(
                ".*Test\\.java",
                SAGAPattern.toRegexp("*Test.java").pattern()
        );
    }
}
