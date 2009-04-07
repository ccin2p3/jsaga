package integration;

import junit.framework.Test;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MailIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MailIntegrationTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new MailIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(MailIntegrationTestSuite.class);}}

    /** test cases */
    public static class MailNSEntryTest extends NSEntryTest {
        public MailNSEntryTest() throws Exception {super("mail");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
}
