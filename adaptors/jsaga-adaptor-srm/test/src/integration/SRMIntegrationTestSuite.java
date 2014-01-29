package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.permissions.PermTest;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMIntegrationTestSuite
* Author: lionel.schwarz@in2p3.fr
* Date:   29 JAN 2014
* ***************************************************
* Description:                                      */
@RunWith(Suite.class)
@SuiteClasses({
    SRMIntegrationTestSuite.SRMNSEntryTest.class,
    SRMIntegrationTestSuite.SRMDirectoryMakeTest.class,
    SRMIntegrationTestSuite.SRMDirectoryTest.class,
    SRMIntegrationTestSuite.SRMFileReadTest.class,
    SRMIntegrationTestSuite.SRMFileWriteTest.class,
    SRMIntegrationTestSuite.SRMDataMovementTest.class,
    SRMIntegrationTestSuite.SRMEmulatorDataMovementTest.class,
    SRMIntegrationTestSuite.SRMPermissionsTest.class,
    })
/**
 *
 */
public class SRMIntegrationTestSuite {
    private final static String TYPE = "srm";

    /** test cases */
    public static class SRMCleanUp extends DataCleanUp {
        public SRMCleanUp() throws Exception {super(TYPE, TYPE);}
    }
    public static class SRMNSSetUpTest extends SetUpTest {
        public SRMNSSetUpTest() throws Exception {super(TYPE);}
    }
    public static class SRMNSEntryTest extends EntryTest {
        public SRMNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class SRMDirectoryMakeTest extends MakeDirTest {
        public SRMDirectoryMakeTest() throws Exception {super(TYPE);}
    }
    public static class SRMDirectoryTest extends DirTest {
        public SRMDirectoryTest() throws Exception {super(TYPE);}
    }
    public static class SRMFileReadTest extends ReadTest {
        public SRMFileReadTest() throws Exception {super(TYPE);}
        
        @Test @Ignore("comment this line to test big files")
        public void test_size_2GB() throws Exception {
        	// this test only works with srm.base.url=srm://ccsrm02.in2p3.fr:8443/pnfs/in2p3.fr/data/dteam/JSAGA/
        	long size = 2150643248L;
        	URL file2BGURL = createURL(m_dirUrl, "../2BG.data");
            NSEntry file2BG = m_dir.open(file2BGURL, Flags.READ.getValue());
            assertEquals(
                    size,
                    ((File)file2BG).getSize());
        }
    }
    public static class SRMFileWriteTest extends WriteTest {
        public SRMFileWriteTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not supported: Timeout, SRM_request blocked in status SRM_REQUEST_INPROGRESS")
        public void test_read_and_write() throws Exception {}
        @Test @Ignore("Not supported: SRM ends SRM_DUPLICATION_ERROR on SrmPrepareToPut")
        public void test_write_append() throws Exception {}
    }
    public static class SRMDataMovementTest extends DataMovementTest {
        public SRMDataMovementTest() throws Exception {super(TYPE, TYPE);}
    }
    public static class SRMEmulatorDataMovementTest extends DataMovementTest {
        public SRMEmulatorDataMovementTest() throws Exception {super(TYPE, "test");}
    }
    public static class SRMPermissionsTest extends PermTest {
        public SRMPermissionsTest() throws Exception {super(TYPE);}
    }
}
