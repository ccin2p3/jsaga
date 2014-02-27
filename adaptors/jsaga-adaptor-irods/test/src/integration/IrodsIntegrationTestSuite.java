package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.namespace.DataCleanUp;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.namespace.SetUpTest;


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
@RunWith(Suite.class)
@SuiteClasses({
    IrodsIntegrationTestSuite.IrodsNSEntryTest.class,
    IrodsIntegrationTestSuite.IrodsDirectoryMakeTest.class,
    IrodsIntegrationTestSuite.IrodsDirectoryTest.class,
    IrodsIntegrationTestSuite.IrodsFileReadTest.class,
    IrodsIntegrationTestSuite.IrodsFileWriteTest.class,
    IrodsIntegrationTestSuite.Irods_to_EmulatorDataMovementTest.class,
    })
public class IrodsIntegrationTestSuite {

    public static class IrodsCleanUp extends DataCleanUp {
        public IrodsCleanUp() throws Exception {super("irods");}
    }
    public static class IrodsSetUpTest extends SetUpTest {
        public IrodsSetUpTest() throws Exception {super("irods");}
    }
    // test IRODS as a protocol for physical entries
    public static class IrodsNSEntryTest extends EntryTest {
        public IrodsNSEntryTest() throws Exception {super("irods");}
    }
    public static class IrodsDirectoryMakeTest extends MakeDirTest {
        public IrodsDirectoryMakeTest() throws Exception {super("irods");}
    }
    public static class IrodsDirectoryTest extends DirTest {
        public IrodsDirectoryTest() throws Exception {super("irods");}
    }
    public static class IrodsFileReadTest extends ReadTest {
        public IrodsFileReadTest() throws Exception {super("irods");}
    }
    public static class IrodsFileWriteTest extends WriteTest {
        public IrodsFileWriteTest() throws Exception {super("irods");}
    }
    public static class Irods_to_EmulatorDataMovementTest extends DataMovementTest {
        public Irods_to_EmulatorDataMovementTest() throws Exception {super("irods", "test");}
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
