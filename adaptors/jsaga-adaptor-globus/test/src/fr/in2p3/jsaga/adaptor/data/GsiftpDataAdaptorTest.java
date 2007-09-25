package fr.in2p3.jsaga.adaptor.data;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDataAdaptorTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "gsiftp",
                new GsiftpDataAdaptor().getSchemeAliases()[0]);
    }
}
