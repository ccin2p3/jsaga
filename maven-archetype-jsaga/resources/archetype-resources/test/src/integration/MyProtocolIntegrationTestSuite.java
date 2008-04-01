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
    public static class MyProtocolNSEntryTest extends NSEntryTest {
        public MyProtocolNSEntryTest() throws Exception {super("myprotocol");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }

    public MyProtocolIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(MyProtocolNSEntryTest.class);
    }

    public static Test suite() throws Exception {
        return new MyProtocolIntegrationTestSuite();
    }
}
