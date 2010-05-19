package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.file.DirectoryListTest;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LinuxIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LinuxIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LinuxIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LinuxIntegrationTestSuite.class);}}

    /** test cases */
    public static class LinuxNSEntryTest extends NSEntryTest {
        public LinuxNSEntryTest() throws Exception {super("linux");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
    public static class LinuxDirectoryListTest extends DirectoryListTest {
        public LinuxDirectoryListTest() throws Exception {super("linux");}
    }
}
