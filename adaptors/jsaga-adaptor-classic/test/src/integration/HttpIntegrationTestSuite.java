package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
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
public class HttpIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new HttpIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(HttpIntegrationTestSuite.class);}}

    /** test cases */
    public static class HttpNSEntryTest extends NSEntryTest {
        public HttpNSEntryTest() throws Exception {super("http");}
    }
    public static class HttpDirectoryListTest extends DirectoryListTest {
        public HttpDirectoryListTest() throws Exception {super("http");}
    }
    public static class HttpDirectoryTest extends DirectoryTest {
        public HttpDirectoryTest() throws Exception {super("http");}
    }
    public static class HttpFileReadTest extends FileReadTest {
        public HttpFileReadTest() throws Exception {super("http");}
    }
    public static class Http_to_EmulatorNSCopyTest extends NSCopyTest {
        public Http_to_EmulatorNSCopyTest() throws Exception {super("http", "test");}
    }
    public static class Http_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Http_to_EmulatorNSCopyRecursiveTest() throws Exception {super("http", "test");}
    }
}
