package integration;

import junit.framework.Test;

import org.ogf.saga.context.ContextDestroyTest;
import org.ogf.saga.context.ContextInfoTest;
import org.ogf.saga.context.ContextInitTest;
import org.ogf.saga.logicalfile.LogicalDirectoryListTest;
import org.ogf.saga.logicalfile.LogicalDirectoryMakeTest;
import org.ogf.saga.logicalfile.LogicalDirectoryMetaDataTest;
import org.ogf.saga.logicalfile.LogicalDirectoryTest;
import org.ogf.saga.logicalfile.LogicalFileReadTest;
import org.ogf.saga.logicalfile.LogicalFileWriteTest;
import org.ogf.saga.namespace.NSCopyFromTest;
import org.ogf.saga.namespace.NSCopyRecursiveTest;
import org.ogf.saga.namespace.NSCopyTest;
import org.ogf.saga.namespace.NSEntryTest;
import org.ogf.saga.namespace.NSLinkTest;
import org.ogf.saga.namespace.NSMoveTest;
import org.ogf.saga.namespace.NSSetUpTest;


/**
 * @author Jerome Revillard
 */
public class LFNIntegrationTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LFNIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LFNIntegrationTestSuite.class);}}

    
    
    /** Init Context (VOMS) */
    public static class A_EGEEContextInit extends ContextInitTest {
        public A_EGEEContextInit() throws Exception {super("EGEE");}
    }
    
    /** Test Context (VOMS) */
    public static class B_EGEEContextInfo extends ContextInfoTest {
        public B_EGEEContextInfo() throws Exception {super();}
    }
    
    /** LFN test cases */
    
    /** OK */
//    public static class C_LFNLogicalDirectoryListTest extends LogicalDirectoryListTest {
//        public C_LFNLogicalDirectoryListTest() throws Exception {super("lfn");}
//        public void test_find() throws Exception {super.test_find();}
//        public void test_find_norecurse() throws Exception {super.test_find_norecurse();}
//        public void test_find_recurse() throws Exception {super.test_find_recurse();}
//        public void test_isFile() throws Exception {super.test_isFile();}
//    }
    
    /** OK */
//    public static class D_LFNLogicalFileReadTest extends LogicalFileReadTest {
//        public D_LFNLogicalFileReadTest() throws Exception {super("lfn");}
//        public void test_listLocations() throws Exception {super.test_listLocations();}
//    }
    
    public static class E_LFNLogicalFileWriteTest extends LogicalFileWriteTest {
        public E_LFNLogicalFileWriteTest() throws Exception {super("lfn");}
        public void test_addLocation() throws Exception { super.test_addLocation(); }
        public void test_removeLocation() throws Exception { super.test_removeLocation(); }
        public void test_updateLocation() throws Exception { super.test_updateLocation(); }
        public void test_replicate() throws Exception { super.test_replicate(); }
    }
    
//    public static class F_LFNLogicalDirectoryTest extends LogicalDirectoryTest {
//        public F_LFNLogicalDirectoryTest() throws Exception {super("lfn");}
//        public void test_openDir() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_openEntry() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class G_LFNLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
//        public G_LFNLogicalDirectoryMetaDataTest() throws Exception {super("lfn");}
//        public void test_listAttributes() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_getAttribute() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_find() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_find_norecurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_find_recurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class H_LFNLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
//        public H_LFNLogicalDirectoryMakeTest() throws Exception {super("lfn");}
//    }
    
//    public static class I_LFNNSSetUpTest extends NSSetUpTest {
//        public I_LFNNSSetUpTest() throws Exception {super("lfn");}
//        public void test_setUp() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSCopyTest extends NSCopyTest {
//        public LFNNSCopyTest() throws Exception {super("lfn", "srm");}
//        public void test_copy() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_and_rename() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_nooverwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_lateExistenceCheck() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSMoveTest extends NSMoveTest {
//        public LFNNSMoveTest() throws Exception {super("lfn", "srm");}
//        public void test_move() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_rename() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_move_recurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSLinkTest extends NSLinkTest {
//        public LFNNSLinkTest() throws Exception {super("lfn");}
//        public void test_isLink() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_readLink() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_link() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_link_dereferenced() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSCopyRecursiveTest extends NSCopyRecursiveTest {
//        public LFNNSCopyRecursiveTest() throws Exception {super("lfn", "srm");}
//        public void test_copy_norecurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse_nooverwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSCopyFromTest extends NSCopyFromTest {
//        public LFNNSCopyFromTest() throws Exception {super("lfn", "srm");}
//        public void test_copy() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFNNSEntryTest extends NSEntryTest {
//        public LFNNSEntryTest() throws Exception {super("lfn");}
//        public void test_getURL() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_getCWD() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_getName() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_unexisting() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFN_to_EmulatorNSCopyTest extends NSCopyTest {
//        public LFN_to_EmulatorNSCopyTest() throws Exception {super("lfn", "test");}
//        public void test_copy() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_and_rename() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_nooverwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_lateExistenceCheck() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFN_to_EmulatorNSMoveTest extends NSMoveTest {
//        public LFN_to_EmulatorNSMoveTest() throws Exception {super("lfn", "test");}
//        public void test_move() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_rename() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_move_recurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFN_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
//        public LFN_to_EmulatorNSCopyRecursiveTest() throws Exception {super("lfn", "test");}
//        public void test_copy_norecurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse_nooverwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_recurse_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
    
//    public static class LFN_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
//        public LFN_to_EmulatorNSCopyFromTest() throws Exception {super("lfn", "test");}
//        public void test_copy() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//        public void test_copy_overwrite() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
//    }
 
    
    /** Destroy Context */
    public static class Z_EGEEContextDestroy extends ContextDestroyTest {
        public Z_EGEEContextDestroy() throws Exception {super("EGEE");}
    }
}
