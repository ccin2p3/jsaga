package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
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
public class MailIntegrationTestSuite extends TestSuite {
    public static class MailNSEntryTest extends NSEntryTest {
        public MailNSEntryTest() throws Exception {super("mail");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }

    public MailIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(MailNSEntryTest.class);
    }

    public static Test suite() throws Exception {
        return new MailIntegrationTestSuite();
    }
}
