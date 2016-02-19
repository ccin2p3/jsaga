package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.namespace.DataCleanUp;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   OpenstackDataTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   18 FEV 2016
****************************************************/

@RunWith(Suite.class)
@SuiteClasses({
	OpenstackDataTestSuite.OpenstackNSEntryTest.class
	,OpenstackDataTestSuite.OpenstackDirTest.class
	,OpenstackDataTestSuite.OpenstackDirectoryMakeTest.class 
	,OpenstackDataTestSuite.OpenstackFileReadTest.class
	,OpenstackDataTestSuite.OpenstackFileWriteTest.class 
//	,OpenstackDataTestSuite.OpenstackDataMovementTest.class 
//	,OpenstackDataTestSuite.OpenstackEmulatorDataMovementTest.class
	})
public class OpenstackDataTestSuite {
	
	private final static String TYPE = "swift";
	
    /** test cases */
    public static class OpenstackNSEntryTest extends EntryTest {
        public OpenstackNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class OpenstackDirTest extends DirTest {
        public OpenstackDirTest() throws Exception {super(TYPE);}
    }
    public static class OpenstackDirectoryMakeTest extends MakeDirTest {
        public OpenstackDirectoryMakeTest() throws Exception {super(TYPE);}
        
        @Test @Override
        public void test_makeDir_child() throws Exception {
            // this does not send DoesNotExistException
            super.test_makeDir_child();
        }
    }
    public static class OpenstackFileReadTest extends ReadTest {
        public OpenstackFileReadTest() throws Exception {super(TYPE);}
    }
    public static class OpenstackFileWriteTest extends WriteTest {
        public OpenstackFileWriteTest() throws Exception {super(TYPE);}

        @Test @Override @Ignore("Not supported")
        public void test_write_encoded_filename() throws Exception {}
        
        @Test(expected=NoSuccessException.class) @Override
        public void test_write_append() throws Exception {
            super.test_write_append();
        }
        
        @Test(expected=java.io.IOException.class) @Override
        public void test_outputStream_append() throws Exception {
            super.test_outputStream_append();
        }
    }
    public static class OpenstackDataMovementTest extends DataMovementTest {
        public OpenstackDataMovementTest() throws Exception {super(TYPE, TYPE);}
    }
    public static class OpenstackEmulatorDataMovementTest extends DataMovementTest {
        public OpenstackEmulatorDataMovementTest() throws Exception {super(TYPE, "test");}
    }
    public static class OpenstackDataCleanUp extends DataCleanUp {
        public OpenstackDataCleanUp() throws Exception {super(TYPE);}
    }
}
