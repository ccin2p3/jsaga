package integration;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.namespace.DataReadOnlyMovementTest;
import org.ogf.saga.namespace.EntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             Https://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpssIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    HttpsIntegrationTestSuite.HttpsNSEntryTest.class,
    HttpsIntegrationTestSuite.HttpsDirectoryTest.class,
    HttpsIntegrationTestSuite.HttpsFileReadTest.class,
    HttpsIntegrationTestSuite.Https_to_EmulatorNSCopyTest.class
})
public class HttpsIntegrationTestSuite {

    protected static String TYPE = "https";

    /** test cases */
    public static class HttpsNSEntryTest extends EntryTest {
        public HttpsNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class HttpsDirectoryTest extends DirTest {
        public HttpsDirectoryTest() throws Exception {super(TYPE);}
        
        @Test(expected=NotImplementedException.class)
        public void test_getSizeRecursive() throws Exception {
            super.test_getSizeRecursive();
        }
        @Test @Ignore("Read-only adaptor: cannot delete file to test empty directory")
        public void test_list_empty() throws Exception {}

    }
    public static class HttpsFileReadTest extends ReadTest {
        public HttpsFileReadTest() throws Exception {super(TYPE);}
    }
    public static class Https_to_EmulatorNSCopyTest extends DataReadOnlyMovementTest {
        public Https_to_EmulatorNSCopyTest() throws Exception {super(TYPE, "test");}
    }
}
