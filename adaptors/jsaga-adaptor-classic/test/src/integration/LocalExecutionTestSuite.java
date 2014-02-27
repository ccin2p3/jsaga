package integration;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InfoTest;
import org.ogf.saga.job.run.InteractiveTest;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
import org.ogf.saga.job.run.RequirementsTest;
import org.ogf.saga.job.run.SandboxTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalExecutionTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/
@RunWith(Suite.class)
@SuiteClasses({
    LocalExecutionTestSuite.LocalJobDescriptionTest.class,
    LocalExecutionTestSuite.LocalJobRunRequiredTest.class,
    LocalExecutionTestSuite.LocalJobRunOptionalTest.class,
    LocalExecutionTestSuite.LocalJobRunDescriptionTest.class,
    LocalExecutionTestSuite.LocalJobRunSandboxTest.class,
    LocalExecutionTestSuite.LocalJobRunInteractiveTest.class,
    LocalExecutionTestSuite.LocalJobRunInfoTest.class
})
public class LocalExecutionTestSuite {

    protected static String TYPE = "local";

    // test cases
    public static class LocalJobDescriptionTest extends DescriptionTest {
        public LocalJobDescriptionTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not supported")
        public void test_spmdVariation() { }
        @Test  @Ignore("Not supported")
        public void test_totalCPUCount() { }
        @Test  @Ignore("Not supported")
        public void test_numberOfProcesses() { }
        @Test  @Ignore("Not supported")
        public void test_processesPerHost() { }
        @Test  @Ignore("Not supported")
        public void test_threadsPerProcess() { }
        @Test  @Ignore("Not supported")
        public void test_input() { }
        @Test  @Ignore("Not supported")
        public void test_output() { }
        @Test  @Ignore("Not supported")
        public void test_error() { }
        @Test  @Ignore("Not supported")
        public void test_fileTransfer() { }
        @Test  @Ignore("Not supported")
        public void test_cleanup() { }
        @Test  @Ignore("Not supported")
        public void test_totalCPUTime() { }
        @Test  @Ignore("Not supported")
        public void test_totalPhysicalMemory() { }
        @Test  @Ignore("Not supported")
        public void test_cpuArchitecture() { }
        @Test  @Ignore("Not supported")
        public void test_operatingSystemType() { }
        @Test  @Ignore("Not supported")
        public void test_candidateHosts() { }
        @Test  @Ignore("Not supported")
        public void test_queue() { }
        @Test  @Ignore("Not supported")
        public void test_wallTimeLimit() { }
     }
    
    // test cases
    public static class LocalJobRunMinimalTest extends MinimalTest {
        public LocalJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class LocalJobRunRequiredTest extends RequiredTest {
        public LocalJobRunRequiredTest() throws Exception {super(TYPE);}
        @Test  @Ignore("test working but too long")
        public void test_run_long() throws Exception {}        
    }

    // test cases
    public static class LocalJobRunSandboxTest extends SandboxTest {
        public LocalJobRunSandboxTest() throws Exception {super(TYPE);}

        @Override
        @Before
        public void setUp() {
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                m_scriptExplicit = m_scriptExplicit.replace("file:", "file:/");
            }
            super.setUp();
        }

        // additional tests to/from remote
        public void test_input_local_to_remote() throws Exception {
            super.runJobExplicit(getLocal("input"), getRemote("input"), getLocal("output"), getWorker("output"));
        }
        public void test_input_remote_to_remote() throws Exception {
            super.runJobExplicit(getRemote("input"), getRemote("i_target"), getLocal("output"), getWorker("output"));
        }
        public void test_output_local_from_remote() throws Exception {
            super.runJobExplicit(getLocal("input"), getWorker("input"), getLocal("output"), getRemote("output"));
        }
        public void test_output_remote_from_remote() throws Exception {
            super.runJobExplicit(getLocal("input"), getWorker("input"), getRemote("output"), getRemote("o_target"));
        }
    }
    
    // test cases
    public static class LocalJobRunOptionalTest extends OptionalTest {
        public LocalJobRunOptionalTest() throws Exception {super(TYPE);}
        @Test  @Ignore("Not supported")
        public void test_resume_done() { }
        @Test  @Ignore("Not supported")
        public void test_suspend_done() { }
        @Test  @Ignore("test working but too long")
        public void test_simultaneousLongJob() throws Exception {}
        @Test  @Ignore("this test hangs")
        public void test_TaskContainer_ShortJob() throws Exception {}
    }
    
 	// test cases
    public static class LocalJobRunDescriptionTest extends RequirementsTest {
        public LocalJobRunDescriptionTest() throws Exception {super(TYPE);}
        @Test @Override @Ignore("Not supported")
        public void test_run_queueRequirement() { }
        @Test @Override @Ignore("Not supported")
        public void test_run_cpuTimeRequirement() { }
        @Test @Override @Ignore("Not supported")
        public void test_run_memoryRequirement() { }
    }
    
    // test cases
    public static class LocalJobRunInteractiveTest extends InteractiveTest {
        public LocalJobRunInteractiveTest() throws Exception {super(TYPE);}
        @Test @Override @Ignore("this test hangs")
        public void test_simultaneousStdin() throws Exception {}
    }
    
    // test cases
    public static class LocalJobRunInfoTest extends InfoTest {
		public LocalJobRunInfoTest() throws Exception {super(TYPE);}
    }
}