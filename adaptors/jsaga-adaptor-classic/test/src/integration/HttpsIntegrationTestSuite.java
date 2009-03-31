package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpsIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpsIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new HttpsIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(HttpsIntegrationTestSuite.class);}}

    /** test cases */
    public static class HttpsNSEntryTest extends NSEntryTest {
        public HttpsNSEntryTest() throws Exception {super("https");}
    }
    public static class HttpsDirectoryListTest extends DirectoryListTest {
        public HttpsDirectoryListTest() throws Exception {super("https");}
    }
    public static class HttpsDirectoryTest extends DirectoryTest {
        public HttpsDirectoryTest() throws Exception {super("https");}
    }
    public static class HttpsFileReadTest extends FileReadTest {
        public HttpsFileReadTest() throws Exception {super("https");}
    }
    public static class Https_to_EmulatorNSCopyTest extends NSCopyTest {
        public Https_to_EmulatorNSCopyTest() throws Exception {super("https", "test");}
    }
    public static class Https_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Https_to_EmulatorNSCopyRecursiveTest() throws Exception {super("https", "test");}
    }
}
