package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbIntegrationTestSuite
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SrbIntegrationTestSuite extends TestSuite {
    public static class SrbNSEntryTest extends NSEntryTest {
        public SrbNSEntryTest() throws Exception {super("srb");}
        public void test_unexisting() { }
    }
	/*
	public static class SrbDirectoryListTest extends DirectoryListTest {
        public SrbDirectoryListTest() throws Exception {
			super("srb");
		}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
*/
    public SrbIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(SrbNSEntryTest.class);
		//this.addTestSuite(SrbDirectoryListTest.class);
    }

    public static Test suite() throws Exception {
        return new SrbIntegrationTestSuite();
    }
}
