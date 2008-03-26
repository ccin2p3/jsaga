package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.logicalfile.*;
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
public class SRMIntegrationTestSuite extends TestSuite {
    public static class SRMNSEntryTest extends NSEntryTest {
        public SRMNSEntryTest() throws Exception {super("srm");}
    }
    public static class SRMLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public SRMLogicalDirectoryListTest() throws Exception {super("srm");}
    }
    public static class SRMLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public SRMLogicalDirectoryMakeTest() throws Exception {super("srm");}
    }
    public static class SRMLogicalDirectoryTest extends LogicalDirectoryTest {
        public SRMLogicalDirectoryTest() throws Exception {super("srm");}
    }
    public static class SRMLogicalFileReadTest extends LogicalFileReadTest {
        public SRMLogicalFileReadTest() throws Exception {super("srm");}
    }
    public static class SRMLogicalFileWriteTest extends LogicalFileWriteTest {
        public SRMLogicalFileWriteTest() throws Exception {super("srm");}
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

    public SRMIntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(SRMNSEntryTest.class);
        this.addTestSuite(SRMLogicalDirectoryListTest.class);
        this.addTestSuite(SRMLogicalDirectoryMakeTest.class);
        this.addTestSuite(SRMLogicalDirectoryTest.class);
        this.addTestSuite(SRMLogicalFileReadTest.class);
        this.addTestSuite(SRMLogicalFileWriteTest.class);

        this.addTestSuite(SRMNSCopyTest.class);
        this.addTestSuite(SRMNSCopyFromTest.class);
        this.addTestSuite(SRMNSCopyRecursiveTest.class);
        this.addTestSuite(SRMNSMoveTest.class);

        this.addTestSuite(SRM_to_EmulatorNSCopyTest.class);
        this.addTestSuite(SRM_to_EmulatorNSCopyFromTest.class);
        this.addTestSuite(SRM_to_EmulatorNSCopyRecursiveTest.class);
        this.addTestSuite(SRM_to_EmulatorNSMoveTest.class);
    }

    public static Test suite() throws Exception {
        return new SRMIntegrationTestSuite();
    }
}
