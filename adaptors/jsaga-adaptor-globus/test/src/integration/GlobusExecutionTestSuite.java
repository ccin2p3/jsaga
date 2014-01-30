package integration;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.*;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InteractiveTest;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
import org.ogf.saga.job.run.SandboxTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusExecutionTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    GlobusExecutionTestSuite.GlobusJobRunRequiredTest.class,
    GlobusExecutionTestSuite.GlobusJobRunOptionalTest.class,
    GlobusExecutionTestSuite.GlobusJobDescriptionTest.class,
    GlobusExecutionTestSuite.GlobusJobRunDescriptionTest.class,
    GlobusExecutionTestSuite.GlobusJobRunSandboxTest.class,
    GlobusExecutionTestSuite.GlobusJobRunInteractiveTest.class,
    GlobusExecutionTestSuite.GlobusJobRunWithPrequisiteTest.class
    })
public class GlobusExecutionTestSuite {

    protected static String TYPE;

    @BeforeClass
    public static void setType() {
        TYPE = "gatekeeper";
    }

    // test cases
    public static class GlobusJobDescriptionTest extends DescriptionTest {
        public GlobusJobDescriptionTest() throws Exception {super(TYPE);}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_totalCPUCount() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_threadsPerProcess() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_fileTransfer() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_cleanup() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_cpuArchitecture() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_operatingSystemType() { }
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_candidateHosts() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_wallTimeLimit() {}
    }

    // test cases
    public static class GlobusJobRunMinimalTest extends MinimalTest {
        public GlobusJobRunMinimalTest() throws Exception {super(TYPE);}
     }
    
    // test cases
    public static class GlobusJobRunRequiredTest extends RequiredTest {
        public GlobusJobRunRequiredTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class GlobusJobRunSandboxTest extends SandboxTest {
        public GlobusJobRunSandboxTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class GlobusJobRunOptionalTest extends OptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not supported")
        public void test_resume_done() {}
        @Test @Ignore("Not supported")
        public void test_resume_running() {}
        @Test @Ignore("Not supported")
        public void test_suspend_done() {}
        @Test @Ignore("Not supported")
        public void test_suspend_running() {}
        @Test @Ignore("not supported by adaptor but MUST BE REACTIVATED when supported by the engine")
        public void test_listJob() {}
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends DescriptionTest {
        public GlobusJobRunDescriptionTest() throws Exception {super(TYPE);}
        @Test @Ignore("Unexpected error: The job manager failed to open stdout")
        public void test_run_inWorkingDirectory() { } 
    }

    public static class GlobusJobRunInteractiveTest extends InteractiveTest {
        public GlobusJobRunInteractiveTest() throws Exception {super(TYPE);}
    }

    public static class GlobusJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public GlobusJobRunWithPrequisiteTest() throws Exception {super(TYPE);}
    }
}