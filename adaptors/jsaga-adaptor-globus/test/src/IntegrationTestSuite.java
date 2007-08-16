import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IntegrationTestSuite extends TestSuite {
    public static class GsiftpNSEntryTest extends NSEntryTest {
        public GsiftpNSEntryTest() throws Exception {super("gsiftp");}
    }

    public IntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(GsiftpNSEntryTest.class);
    }

    public static Test suite() throws Exception {
        return new IntegrationTestSuite();
    }
}