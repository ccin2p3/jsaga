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

    public IrodsIntegrationTestSuite() throws Exception {
        super();
        // test SRB as a protocol for physical entries
        this.addTestSuite(IrodsNSEntryTest.class);
		this.addTestSuite(IrodsDirectoryListTest.class);
        this.addTestSuite(IrodsDirectoryMakeTest.class);
        this.addTestSuite(IrodsDirectoryTest.class);
        this.addTestSuite(IrodsFileReadTest.class);
        this.addTestSuite(IrodsFileWriteTest.class);
        this.addTestSuite(Irods_to_EmulatorNSCopyTest.class);
        this.addTestSuite(Irods_to_EmulatorNSCopyFromTest.class);
        this.addTestSuite(Irods_to_EmulatorNSCopyRecursiveTest.class);
        this.addTestSuite(Irods_to_EmulatorNSMoveTest.class);

/*
        // test SRB as a protocol for logical entries
        this.addTestSuite(IrodsLogicalDirectoryListTest.class);
        this.addTestSuite(IrodsLogicalDirectoryMakeTest.class);
        this.addTestSuite(IrodsLogicalDirectoryMetaDataTest.class);
        this.addTestSuite(IrodsLogicalDirectoryTest.class);
        this.addTestSuite(IrodsLogicalFileReadTest.class);
        this.addTestSuite(IrodsLogicalFileWriteTest.class);
*/
    }

    public static Test suite() throws Exception {
        return new IrodsIntegrationTestSuite();
    }
}
