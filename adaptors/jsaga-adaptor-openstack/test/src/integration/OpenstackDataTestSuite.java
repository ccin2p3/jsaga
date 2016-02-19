package integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
import org.ogf.saga.namespace.DataCleanUp;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.impl.file.copy.AbstractCopyTask;

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
//	,OpenstackDataTestSuite.OpenstackDirectoryMakeTest.class 
//	,OpenstackDataTestSuite.OpenstackFileReadTest.class
//	,OpenstackDataTestSuite.OpenstackFileWriteTest.class 
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
        @Test @Ignore
        public void test_getSizeRecursive() throws Exception {
        }
    }
    public static class OpenstackDirectoryMakeTest extends MakeDirTest {
        public OpenstackDirectoryMakeTest() throws Exception {super(TYPE);}
    }
    public static class OpenstackFileReadTest extends ReadTest {
        public OpenstackFileReadTest() throws Exception {super(TYPE);}
    }
    public static class OpenstackFileWriteTest extends WriteTest {
        public OpenstackFileWriteTest() throws Exception {super(TYPE);}
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
