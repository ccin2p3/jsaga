package fr.in2p3.jsaga.adaptor.data;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SRMDataAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SRMDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "srm",
                new SRMDataAdaptor().getSchemeAliases()[0]);
    }
}
