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
import org.ogf.saga.permissions.PermissionsTest;


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
    public static class LFNLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public LFNLogicalDirectoryListTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class D_LFNLogicalFileReadTest extends LogicalFileReadTest {
        public D_LFNLogicalFileReadTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFNLogicalFileWriteTest extends LogicalFileWriteTest {
        public LFNLogicalFileWriteTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFNLogicalDirectoryTest extends LogicalDirectoryTest {
        public LFNLogicalDirectoryTest() throws Exception {super("lfn");}
    }
    
    /** NOT IMPLEMENTED YET */
//    public static class LFNLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
//        public LFNLogicalDirectoryMetaDataTest() throws Exception {super("lfn");}
//        public void test_listAttributes() throws Exception {super.ignore("Not implemented yet.");}
//        public void test_getAttribute() throws Exception {super.ignore("Not implemented yet.");}
//        public void test_find() throws Exception {super.ignore("Not implemented yet.");}
//        public void test_find_norecurse() throws Exception {super.ignore("Not implemented yet.");}
//        public void test_find_recurse() throws Exception {super.ignore("Not implemented yet.");}
//    }
    
    /** OK */
    public static class LFNLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public LFNLogicalDirectoryMakeTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFNNSSetUpTest extends NSSetUpTest {
        public LFNNSSetUpTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFNNSCopyTest extends NSCopyTest {
        public LFNNSCopyTest() throws Exception {super("lfn", "lfn");}
    }
    
    /** OK */
    public static class LFNNSMoveTest extends NSMoveTest {
        public LFNNSMoveTest() throws Exception {super("lfn", "lfn");}
    }
    
    /** OK */
    public static class LFNNSLinkTest extends NSLinkTest {
        public LFNNSLinkTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFNNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public LFNNSCopyRecursiveTest() throws Exception {super("lfn", "lfn");}
    }
    
    /** NOT OK */
    public static class LFNNSCopyFromTest extends NSCopyFromTest {
        public LFNNSCopyFromTest() throws Exception {super("lfn", "lfn");}
    }
    
    /** OK */
    public static class LFNNSEntryTest extends NSEntryTest {
        public LFNNSEntryTest() throws Exception {super("lfn");}
    }
    
    /** OK */
    public static class LFN_to_EmulatorNSCopyTest extends NSCopyTest {
        public LFN_to_EmulatorNSCopyTest() throws Exception {super("lfn", "test");}
    }
    
    /** OK */
    public static class LFN_to_EmulatorNSMoveTest extends NSMoveTest {
        public LFN_to_EmulatorNSMoveTest() throws Exception {super("lfn", "test");}
    }
    
    /** OK */
    public static class LFN_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public LFN_to_EmulatorNSCopyRecursiveTest() throws Exception {super("lfn", "test");}
    }
    
    /** NOT OK */
//    public static class LFN_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
//        public LFN_to_EmulatorNSCopyFromTest() throws Exception {super("lfn", "test");}
//    }
 
    /** OK */
    public static class LFNPermissionsTest extends PermissionsTest {
        public LFNPermissionsTest() throws Exception {super("lfn");}
    }
    
    /** Destroy Context */
    public static class Z_EGEEContextDestroy extends ContextDestroyTest {
        public Z_EGEEContextDestroy() throws Exception {super("EGEE");}
    }
}
