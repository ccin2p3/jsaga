package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.logicalfile.LogicalDirTest;
import org.ogf.saga.logicalfile.LogicalMakeDirTest;
import org.ogf.saga.logicalfile.LogicalReadTest;
import org.ogf.saga.logicalfile.LogicalWriteTest;
import org.ogf.saga.namespace.DataCleanUp;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.namespace.LinkTest;
import org.ogf.saga.namespace.SetUpTest;
import org.ogf.saga.permissions.PermissionsTest;


/**
 * @author Jerome Revillard
 */
@RunWith(Suite.class)
@SuiteClasses({
    LFNIntegrationTestSuite.LFNNSEntryTest.class,
    LFNIntegrationTestSuite.LFNNSLinkTest.class,
    LFNIntegrationTestSuite.LFNLogicalFileReadTest.class,
    LFNIntegrationTestSuite.LFNLogicalFileWriteTest.class,
    LFNIntegrationTestSuite.LFNLogicalDirectoryTest.class,
    LFNIntegrationTestSuite.LFNLogicalDirectoryMakeTest.class,
    LFNIntegrationTestSuite.LFNDataMovementTest.class,
    LFNIntegrationTestSuite.LFN_to_EmulatorDataMovementTest.class,
    LFNIntegrationTestSuite.LFNPermissionsTest.class
})
public class LFNIntegrationTestSuite {

    public static class LFNNSSetUpTest extends SetUpTest {
        public LFNNSSetUpTest() throws Exception {super("lfn");}
    }
    
    public class LFNCleanup extends DataCleanUp {
        public LFNCleanup() throws Exception {
            super("lfn");
        }
    }
    
    public static class LFNNSEntryTest extends EntryTest {
        public LFNNSEntryTest() throws Exception {super("lfn");}
    }
    
    public static class LFNNSLinkTest extends LinkTest {
        public LFNNSLinkTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalDirectoryTest extends LogicalDirTest {
        public LFNLogicalDirectoryTest() throws Exception {super("lfn");}
    }
    

// Not supported by the LFC protocol
//    public static class LFNLogicalDirectoryMetaDataTest extends LogicalDirMetaDataTest {
//        public LFNLogicalDirectoryMetaDataTest() throws Exception {super("lfn");}
//    }
    
    public static class LFNLogicalDirectoryMakeTest extends LogicalMakeDirTest {
        public LFNLogicalDirectoryMakeTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalFileReadTest extends LogicalReadTest {
        public LFNLogicalFileReadTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalFileWriteTest extends LogicalWriteTest {
        public LFNLogicalFileWriteTest() throws Exception {super("lfn");}
    }
    
    public static class LFNDataMovementTest extends DataMovementTest {
        public LFNDataMovementTest() throws Exception {super("lfn", "lfn");}
        @Override @Test @Ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot belong to 2 different LFC entries.")
        public void test_copy_recurse() throws Exception {}
        
        @Override @Test @Ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot belong to 2 different LFC entries.")
        public void test_copy_recurse_overwrite() throws Exception {}

        @Override @Test @Ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot belong to 2 different LFC entries.")
        public void test_copy() throws Exception {}

        @Override @Test @Ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot belong to 2 different LFC entries.")
        public void test_copy_overwrite() throws Exception {}
        
        @Override @Test @Ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot belong to 2 different LFC entries.")
        public void test_copy_and_rename() throws Exception {}
    }
    

    public static class LFN_to_EmulatorDataMovementTest extends DataMovementTest {
        public LFN_to_EmulatorDataMovementTest() throws Exception {super("lfn", "test");}
    }
    
    public static class LFNPermissionsTest extends PermissionsTest {
        public LFNPermissionsTest() throws Exception {super("lfn");}
    }
}
