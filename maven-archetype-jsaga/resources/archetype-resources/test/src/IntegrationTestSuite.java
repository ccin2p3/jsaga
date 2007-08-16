import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IntegrationTestSuite extends TestSuite {
    public static class MyProtocolNSEntryTest extends NSEntryTest {
        public MyProtocolNSEntryTest() throws Exception {super("myprotocol");}
    }

    public IntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(MyProtocolNSEntryTest.class);
    }

    public static Test suite() throws Exception {
        return new IntegrationTestSuite();
    }
}
