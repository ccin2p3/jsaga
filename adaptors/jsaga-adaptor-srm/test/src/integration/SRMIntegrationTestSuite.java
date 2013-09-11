package integration;

import junit.framework.Test;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.permissions.PermissionsTest;
import org.ogf.saga.url.URL;

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
    public static class SRMNSSetUpTest extends NSSetUpTest {
        public SRMNSSetUpTest() throws Exception {super("srm");}
    }
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
        /*
        public void test_size_2GB() throws Exception {
        	// this test only works with srm.base.url=srm://ccsrm02.in2p3.fr:8443/pnfs/in2p3.fr/data/dteam/JSAGA/
        	long size = 2150643248L;
        	URL file2BGURL = createURL(m_dirUrl, "../2BG.data");
            NSEntry file2BG = m_dir.open(file2BGURL, Flags.READ.getValue());
            assertEquals(
                    size,
                    ((File)file2BG).getSize());
        }
        */
    }
    public static class SRMFileWriteTest extends FileWriteTest {
        public SRMFileWriteTest() throws Exception {super("srm");}
        public void test_read_and_write() throws Exception {super.ignore("Not supported: Timeout, SRM_request blocked in status SRM_REQUEST_INPROGRESS");}
        public void test_write_append() throws Exception {super.ignore("Not supported: SRM ends SRM_DUPLICATION_ERROR on SrmPrepareToPut");}
    }
    public static class SRMNSCopyTest extends NSCopyTest {
        public SRMNSCopyTest() throws Exception {super("srm", "srm");}
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
    public static class SRM_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public SRM_to_EmulatorNSCopyRecursiveTest() throws Exception {super("srm", "test");}
    }
    public static class SRM_to_EmulatorNSMoveTest extends NSMoveTest {
        public SRM_to_EmulatorNSMoveTest() throws Exception {super("srm", "test");}
    }
    public static class SRMPermissionsTest extends PermissionsTest {
        public SRMPermissionsTest() throws Exception {super("srm");}
    }
}
