package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
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
public class ZipIntegrationTestSuite extends TestSuite {
    public static class ZipNSEntryTest extends NSEntryTest {
        public ZipNSEntryTest() throws Exception {super("zip");}
    }
    public static class ZipDirectoryListTest extends DirectoryListTest {
        public ZipDirectoryListTest() throws Exception {super("zip");}
    }
    public static class ZipDirectoryMakeTest extends DirectoryMakeTest {
        public ZipDirectoryMakeTest() throws Exception {super("zip");}
    }
    public static class ZipDirectoryTest extends DirectoryTest {
        public ZipDirectoryTest() throws Exception {super("zip");}
    }
    public static class ZipFileReadTest extends FileReadTest {
        public ZipFileReadTest() throws Exception {super("zip");}
    }
    public static class ZipFileWriteTest extends FileWriteTest {
        public ZipFileWriteTest() throws Exception {super("zip");}
    }
    public static class ZipNSCopyTest extends NSCopyTest {
        public ZipNSCopyTest() throws Exception {super("zip", "zip");}
    }
    public static class ZipNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public ZipNSCopyRecursiveTest() throws Exception {super("zip", "zip");}
    }
    public static class ZipNSMoveTest extends NSMoveTest {
        public ZipNSMoveTest() throws Exception {super("zip", "zip");}
    }
    public static class Zip_to_EmulatorNSCopyTest extends NSCopyTest {
        public Zip_to_EmulatorNSCopyTest() throws Exception {super("zip", "test");}
    }
    public static class Zip_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Zip_to_EmulatorNSCopyRecursiveTest() throws Exception {super("zip", "test");}
    }
    public static class Zip_to_EmulatorNSMoveTest extends NSMoveTest {
        public Zip_to_EmulatorNSMoveTest() throws Exception {super("zip", "test");}
    }

    public ZipIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(ZipNSEntryTest.class);
        this.addTestSuite(ZipDirectoryListTest.class);
//        this.addTestSuite(ZipDirectoryMakeTest.class);
        this.addTestSuite(ZipDirectoryTest.class);
        this.addTestSuite(ZipFileReadTest.class);
//        this.addTestSuite(ZipFileWriteTest.class);
//        this.addTestSuite(ZipNSCopyTest.class);
//        this.addTestSuite(ZipNSCopyRecursiveTest.class);
//        this.addTestSuite(ZipNSMoveTest.class);
        this.addTestSuite(Zip_to_EmulatorNSCopyTest.class);
        this.addTestSuite(Zip_to_EmulatorNSCopyRecursiveTest.class);
//        this.addTestSuite(Zip_to_EmulatorNSMoveTest.class);
    }

    public static Test suite() throws Exception {
        return new ZipIntegrationTestSuite();
    }
}
