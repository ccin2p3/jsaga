package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.logicalfile.LogicalDirTest;
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
//    IrodsLogicalIntegrationTestSuite.IrodsNSEntryTest.class,
//    IrodsLogicalIntegrationTestSuite.IrodsDirectoryMakeTest.class,
    IrodsLogicalIntegrationTestSuite.IrodsLogicalDirectoryTest.class,
//    IrodsLogicalIntegrationTestSuite.IrodsFileReadTest.class,
//    IrodsLogicalIntegrationTestSuite.IrodsFileWriteTest.class,
//    IrodsLogicalIntegrationTestSuite.Irods_to_EmulatorDataMovementTest.class
    })
public class IrodsLogicalIntegrationTestSuite {

    // test IRODS as a protocol for logical entries
    public static class IrodsLogicalDirectoryTest extends LogicalDirTest {
        public IrodsLogicalDirectoryTest() throws Exception {super("irodsl");}
    }
//    public static class IrodsLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
//        public IrodsLogicalDirectoryMakeTest() throws Exception {super("irodsl");}
//    }
//    public static class IrodsLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
//        public IrodsLogicalDirectoryMetaDataTest() throws Exception {super("irodsl");}
//    }
//    public static class IrodsLogicalDirectoryTest extends LogicalDirectoryTest {
//        public IrodsLogicalDirectoryTest() throws Exception {super("irodsl");}
//    }
//    public static class IrodsLogicalFileReadTest extends LogicalFileReadTest {
//        public IrodsLogicalFileReadTest() throws Exception {super("irodsl");}
//    }
//    public static class IrodsLogicalFileWriteTest extends LogicalFileWriteTest {
//        public IrodsLogicalFileWriteTest() throws Exception {super("irodsl");}
//    }
}
