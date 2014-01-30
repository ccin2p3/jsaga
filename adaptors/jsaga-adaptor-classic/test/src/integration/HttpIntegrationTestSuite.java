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
* File:   HttpIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    HttpIntegrationTestSuite.HttpNSEntryTest.class,
    HttpIntegrationTestSuite.HttpDirectoryTest.class,
    HttpIntegrationTestSuite.HttpFileReadTest.class,
    HttpIntegrationTestSuite.Http_to_EmulatorNSCopyTest.class
})
public class HttpIntegrationTestSuite {

    protected static String TYPE = "http";

    /** test cases */
    public static class HttpNSEntryTest extends EntryTest {
        public HttpNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class HttpDirectoryTest extends DirTest {
        public HttpDirectoryTest() throws Exception {super(TYPE);}
        
        @Test(expected=NotImplementedException.class)
        public void test_getSizeRecursive() throws Exception {
            super.test_getSizeRecursive();
        }
        @Test @Ignore("Read-only adaptor: cannot delete file to test empty directory")
        public void test_list_empty() throws Exception {}

    }
    public static class HttpFileReadTest extends ReadTest {
        public HttpFileReadTest() throws Exception {super(TYPE);}
    }
    public static class Http_to_EmulatorNSCopyTest extends DataReadOnlyMovementTest {
        public Http_to_EmulatorNSCopyTest() throws Exception {super(TYPE, "test");}
    }
}
