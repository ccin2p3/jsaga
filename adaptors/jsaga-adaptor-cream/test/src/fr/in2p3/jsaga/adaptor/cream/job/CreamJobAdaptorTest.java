package fr.in2p3.jsaga.adaptor.cream.job;

import junit.framework.TestCase;
import fr.in2p3.jsaga.adaptor.cream.job.CreamJobControlAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamJobAdaptorTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamJobAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "cream",
                new CreamJobControlAdaptor().getType());
    }
}
