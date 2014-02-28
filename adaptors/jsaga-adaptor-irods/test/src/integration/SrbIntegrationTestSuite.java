package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;


/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbIntegrationTestSuite
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    SrbIntegrationTestSuite.SrbNSEntryTest.class,
    SrbIntegrationTestSuite.SrbDirectoryMakeTest.class,
    SrbIntegrationTestSuite.SrbDirectoryTest.class,
    SrbIntegrationTestSuite.SrbFileReadTest.class,
    SrbIntegrationTestSuite.SrbFileWriteTest.class,
    SrbIntegrationTestSuite.Srb_to_EmulatorDataMovementTest.class
    })
public class SrbIntegrationTestSuite{

    // test SRB as a protocol for physical entries
    public static class SrbNSEntryTest extends EntryTest {
        public SrbNSEntryTest() throws Exception {super("srb");}
    }
    public static class SrbDirectoryMakeTest extends MakeDirTest {
        public SrbDirectoryMakeTest() throws Exception {super("srb");}
    }
    public static class SrbDirectoryTest extends DirTest {
        public SrbDirectoryTest() throws Exception {super("srb");}
    }
    public static class SrbFileReadTest extends ReadTest {
        public SrbFileReadTest() throws Exception {super("srb");}
    }
    public static class SrbFileWriteTest extends WriteTest {
        public SrbFileWriteTest() throws Exception {super("srb");}
    }
    public static class Srb_to_EmulatorDataMovementTest extends DataMovementTest {
        public Srb_to_EmulatorDataMovementTest() throws Exception {super("srb", "test");}
    }

/*
    // test SRB as a protocol for logical entries
    public static class SrbLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public SrbLogicalDirectoryListTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public SrbLogicalDirectoryMakeTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
        public SrbLogicalDirectoryMetaDataTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryTest extends LogicalDirectoryTest {
        public SrbLogicalDirectoryTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalFileReadTest extends LogicalFileReadTest {
        public SrbLogicalFileReadTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalFileWriteTest extends LogicalFileWriteTest {
        public SrbLogicalFileWriteTest() throws Exception {super("srb");}
    }
*/
}
