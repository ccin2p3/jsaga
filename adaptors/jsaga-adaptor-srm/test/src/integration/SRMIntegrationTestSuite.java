package integration;

import junit.framework.Test;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SRMIntegrationTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new SRMIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(SRMIntegrationTestSuite.class);}}

    /** test cases */
    public static class SRMNSEntryTest extends NSEntryTest {
        public SRMNSEntryTest() throws Exception {super("srm");}
    }
    public static class SRMDirectoryListTest extends DirectoryListTest {
        public SRMDirectoryListTest() throws Exception {super("srm");}
    }
    public static class SRMDirectoryMakeTest extends DirectoryMakeTest {
        public SRMDirectoryMakeTest() throws Exception {super("srm");}
    }
    public static class SRMDirectoryTest extends DirectoryTest {
        public SRMDirectoryTest() throws Exception {super("srm");}
    }
    public static class SRMFileReadTest extends FileReadTest {
        public SRMFileReadTest() throws Exception {super("srm");}
    }
    public static class SRMFileWriteTest extends FileWriteTest {
        public SRMFileWriteTest() throws Exception {super("srm");}
    }
    public static class SRMNSCopyTest extends NSCopyTest {
        public SRMNSCopyTest() throws Exception {super("srm", "srm");}
    }
    public static class SRMNSCopyFromTest extends NSCopyFromTest {
        public SRMNSCopyFromTest() throws Exception {super("srm", "srm");}
    }
    public static class SRMNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public SRMNSCopyRecursiveTest() throws Exception {super("srm", "srm");}
    }
    /** FIXME: "SRM_FAILURE: Incompatible with current file status" */
    public static class SRMNSMoveTest extends NSMoveTest {
        public SRMNSMoveTest() throws Exception {super("srm", "srm");}
    }
    public static class SRM_to_EmulatorNSCopyTest extends NSCopyTest {
        public SRM_to_EmulatorNSCopyTest() throws Exception {super("srm", "test");}
    }
    public static class SRM_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
        public SRM_to_EmulatorNSCopyFromTest() throws Exception {super("srm", "test");}
    }
    public static class SRM_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public SRM_to_EmulatorNSCopyRecursiveTest() throws Exception {super("srm", "test");}
    }
    public static class SRM_to_EmulatorNSMoveTest extends NSMoveTest {
        public SRM_to_EmulatorNSMoveTest() throws Exception {super("srm", "test");}
    }
}
