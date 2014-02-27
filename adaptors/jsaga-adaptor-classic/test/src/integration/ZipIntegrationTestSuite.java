package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ZipIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    ZipIntegrationTestSuite.ZipNSEntryTest.class,
    ZipIntegrationTestSuite.ZipDirectoryTest.class,
    ZipIntegrationTestSuite.ZipFileReadTest.class,
    ZipIntegrationTestSuite.Zip_to_EmulatorNSCopyTest.class
})
public class ZipIntegrationTestSuite {

    protected static String TYPE = "zip";

    /** test cases */
    public static class ZipNSEntryTest extends EntryTest {
        public ZipNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class ZipDirectoryTest extends DirTest {
        public ZipDirectoryTest() throws Exception {super(TYPE);}
        @Test(expected=NotImplementedException.class)
        public void test_getSizeRecursive() throws Exception {
            super.test_getSizeRecursive();
        }
        @Test @Ignore("Read-only adaptor: cannot delete file to test empty directory")
        public void test_list_empty() throws Exception {}
    }
    public static class ZipFileReadTest extends ReadTest {
        public ZipFileReadTest() throws Exception {super(TYPE);}
    }
    public static class Zip_to_EmulatorNSCopyTest extends DataReadOnlyMovementTest {
        public Zip_to_EmulatorNSCopyTest() throws Exception {super(TYPE, "test");}
    }
}
