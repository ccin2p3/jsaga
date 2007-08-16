package ${package}.data;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   MyProtocolDataAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class MyProtocolDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "myprotocol",
                new MyProtocolDataAdaptor().getScheme());
    }
}
