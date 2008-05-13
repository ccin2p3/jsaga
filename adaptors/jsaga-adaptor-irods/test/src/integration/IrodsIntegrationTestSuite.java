package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsIntegrationTestSuite
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IrodsIntegrationTestSuite extends TestSuite {
    public static class IrodsNSEntryTest extends NSEntryTest {
        public IrodsNSEntryTest() throws Exception {super("irods");}
        public void test_unexisting() { }
    }
	/*
	public static class IrodsDirectoryListTest extends DirectoryListTest {
        public IrodsDirectoryListTest() throws Exception {
			super("irods");
		}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
*/
    public IrodsIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(IrodsNSEntryTest.class);
		//this.addTestSuite(IrodsDirectoryListTest.class);
    }

    public static Test suite() throws Exception {
        return new IrodsIntegrationTestSuite();
    }
}
