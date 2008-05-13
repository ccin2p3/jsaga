package fr.in2p3.jsaga.adaptor.data;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsDataAdaptorTest
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IrodsDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "irods",
                new IrodsDataAdaptor().getType());
    }
}
