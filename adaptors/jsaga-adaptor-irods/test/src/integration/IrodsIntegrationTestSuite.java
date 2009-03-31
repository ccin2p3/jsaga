package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

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
    /** create test suite */
    public static Test suite() throws Exception {return new IrodsIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(IrodsIntegrationTestSuite.class);}}

    // test IRODS as a protocol for physical entries
    public static class IrodsNSEntryTest extends NSEntryTest {
        public IrodsNSEntryTest() throws Exception {super("irods");}
    }
	public static class IrodsDirectoryListTest extends DirectoryListTest {
        public IrodsDirectoryListTest() throws Exception {super("irods");}
    }
    public static class IrodsDirectoryMakeTest extends DirectoryMakeTest {
        public IrodsDirectoryMakeTest() throws Exception {super("irods");}
    }
    public static class IrodsDirectoryTest extends DirectoryTest {
        public IrodsDirectoryTest() throws Exception {super("irods");}
    }
    public static class IrodsFileReadTest extends FileReadTest {
        public IrodsFileReadTest() throws Exception {super("irods");}
    }
    public static class IrodsFileWriteTest extends FileWriteTest {
        public IrodsFileWriteTest() throws Exception {super("irods");}
    }
    public static class Irods_to_EmulatorNSCopyTest extends NSCopyTest {
        public Irods_to_EmulatorNSCopyTest() throws Exception {super("irods", "test");}
    }
    public static class Irods_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
        public Irods_to_EmulatorNSCopyFromTest() throws Exception {super("irods", "test");}
    }
    public static class Irods_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Irods_to_EmulatorNSCopyRecursiveTest() throws Exception {super("irods", "test");}
    }
    public static class Irods_to_EmulatorNSMoveTest extends NSMoveTest {
        public Irods_to_EmulatorNSMoveTest() throws Exception {super("irods", "test");}
    }

/*
    // test IRODS as a protocol for logical entries
    public static class IrodsLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public IrodsLogicalDirectoryListTest() throws Exception {super("irods");}
    }
    public static class IrodsLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public IrodsLogicalDirectoryMakeTest() throws Exception {super("irods");}
    }
    public static class IrodsLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
        public IrodsLogicalDirectoryMetaDataTest() throws Exception {super("irods");}
    }
    public static class IrodsLogicalDirectoryTest extends LogicalDirectoryTest {
        public IrodsLogicalDirectoryTest() throws Exception {super("irods");}
    }
    public static class IrodsLogicalFileReadTest extends LogicalFileReadTest {
        public IrodsLogicalFileReadTest() throws Exception {super("irods");}
    }
    public static class IrodsLogicalFileWriteTest extends LogicalFileWriteTest {
        public IrodsLogicalFileWriteTest() throws Exception {super("irods");}
    }
*/
}
