package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProtocolIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProtocolIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new MyProtocolIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(MyProtocolIntegrationTestSuite.class);}}

    /** test cases */
    public static class MyProtocolNSEntryTest extends NSEntryTest {
        public MyProtocolNSEntryTest() throws Exception {super("myprotocol");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
}
