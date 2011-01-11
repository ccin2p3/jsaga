package integration;

import junit.framework.Test;

import org.ogf.saga.logicalfile.LogicalDirectoryListTest;
import org.ogf.saga.logicalfile.LogicalDirectoryMakeTest;
import org.ogf.saga.logicalfile.LogicalDirectoryTest;
import org.ogf.saga.logicalfile.LogicalFileReadTest;
import org.ogf.saga.logicalfile.LogicalFileWriteTest;
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

    /** test cases */
    public static class LFNLogicalFileWriteTest extends LogicalFileWriteTest {
        public LFNLogicalFileWriteTest() throws Exception {super("lfn");}
    }
    
// Not supported by the LFC protocol
//    public static class LFNLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
//        public LFNLogicalDirectoryMetaDataTest() throws Exception {super("lfn");}
//    }
    
    public static class LFNLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public LFNLogicalDirectoryMakeTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalFileReadTest extends LogicalFileReadTest {
        public LFNLogicalFileReadTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public LFNLogicalDirectoryListTest() throws Exception {super("lfn");}
    }
    
    public static class LFNLogicalDirectoryTest extends LogicalDirectoryTest {
        public LFNLogicalDirectoryTest() throws Exception {super("lfn");}
    }
    
    public static class LFNNSMoveTest extends NSMoveTest {
        public LFNNSMoveTest() throws Exception {super("lfn", "lfn");}
    }
    
    public static class LFNNSEntryTest extends NSEntryTest {
        public LFNNSEntryTest() throws Exception {super("lfn");}
    }
    
    public static class LFNNSLinkTest extends NSLinkTest {
        public LFNNSLinkTest() throws Exception {super("lfn");}
    }
    
    public static class LFNNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public LFNNSCopyRecursiveTest() throws Exception {super("lfn", "lfn");}
        @Override
        public void test_copy_recurse() throws Exception {
        	super.ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot " +
    		"belong to 2 different LFC entries.");
        }
        
        @Override
        public void test_copy_recurse_overwrite() throws Exception {
        	super.ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot " +
        	"belong to 2 different LFC entries.");
        }
    }
    
    public static class LFNNSCopyTest extends NSCopyTest {
    	public LFNNSCopyTest() throws Exception {super("lfn", "lfn");}
    	@Override
    	public void test_copy() throws Exception {
    		super.ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot " +
    		"belong to 2 different LFC entries.");
    	}
    	@Override
    	public void test_copy_overwrite() throws Exception {
    		super.ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot " +
    		"belong to 2 different LFC entries.");
    	}
    	@Override
    	public void test_copy_and_rename() throws Exception {
    		super.ignore("The LFC maintains a list of replica/location. Then this test fails because a replica/location cannot " +
    		"belong to 2 different LFC entries.");
    	}
    }
    
    public static class LFNNSSetUpTest extends NSSetUpTest {
        public LFNNSSetUpTest() throws Exception {super("lfn");}
    }
    
    public static class LFN_to_EmulatorNSMoveTest extends NSMoveTest {
        public LFN_to_EmulatorNSMoveTest() throws Exception {super("lfn", "test");}
    }
    
    public static class LFN_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public LFN_to_EmulatorNSCopyRecursiveTest() throws Exception {super("lfn", "test");}
    }
    
    public static class LFN_to_EmulatorNSCopyTest extends NSCopyTest {
        public LFN_to_EmulatorNSCopyTest() throws Exception {super("lfn", "test");}
    }
     
    public static class LFNPermissionsTest extends PermissionsTest {
        public LFNPermissionsTest() throws Exception {super("lfn");}
    }
}
