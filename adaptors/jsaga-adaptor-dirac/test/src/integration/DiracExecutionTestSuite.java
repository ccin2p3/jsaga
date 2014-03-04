package integration;

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
import org.ogf.saga.job.run.SandboxTest;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
****************************************************/
@RunWith(Suite.class)
@SuiteClasses({
    DiracExecutionTestSuite.DiracJobDescriptionTest.class,
    DiracExecutionTestSuite.DiracJobRunRequiredTest.class,
    DiracExecutionTestSuite.DiracJobRunOptionalTest.class,
    DiracExecutionTestSuite.DiracJobRunSandboxTest.class,
    DiracExecutionTestSuite.DiracJobRunInteractiveTest.class,
    DiracExecutionTestSuite.DiracJobRunInfoTest.class
})
public class DiracExecutionTestSuite {
	
	private final static String TYPE = "dirac";

    // test cases
    public static class DiracJobDescriptionTest extends DescriptionTest {
        public DiracJobDescriptionTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_spmdVariation() {  }
        @Override @Test @Ignore("Not supported")
        public void test_totalCPUCount() {  }
        @Override @Test @Ignore("Not supported")
        public void test_numberOfProcesses() {  }
        @Override @Test @Ignore("Not supported")
        public void test_processesPerHost() {  }
        @Override @Test @Ignore("Not supported")
        public void test_threadsPerProcess() {  }
        @Override @Test @Ignore("Not supported")
        public void test_cleanup() {  }
        @Override @Test @Ignore("Not supported")
        public void test_totalCPUTime() {  }
        @Override @Test @Ignore("Not supported")
        public void test_totalPhysicalMemory() {  }
        @Override @Test @Ignore("Not supported")
        public void test_cpuArchitecture() {  }
        @Override @Test @Ignore("Not supported")
        public void test_operatingSystemType() {  }
        @Override @Test @Ignore("Not supported")
        public void test_candidateHosts() {  }
        @Override @Test @Ignore("Not supported")
        public void test_queue() {  }
        @Override @Test @Ignore("Not supported")
        public void test_wallTimeLimit() {  }
     }
    
    // test cases
    public static class DiracJobRunMinimalTest extends MinimalTest {
        public DiracJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class DiracJobRunRequiredTest extends RequiredTest {
        public DiracJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class DiracJobRunSandboxTest extends SandboxTest {
        public DiracJobRunSandboxTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_output_workingDirectory() {  }
    }
    
    // test cases
    public static class DiracJobRunOptionalTest extends OptionalTest {
        public DiracJobRunOptionalTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {  }
    }
    
 	// test cases
//    public static class DiracJobRunDescriptionTest extends RequirementsTest {
//        public DiracJobRunDescriptionTest() throws Exception {super(TYPE);}
//        public void test_run_queueRequirement() {  }
//        public void test_run_cpuTimeRequirement() {  }
//        public void test_run_memoryRequirement() {  }
//    }
    
    // test cases
    public static class DiracJobRunInteractiveTest extends InteractiveTest {
        public DiracJobRunInteractiveTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_simultaneousStdin()  { }
    }

    // test cases
    public static class DiracJobRunInfoTest extends InfoTest {
        public DiracJobRunInfoTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_exitcode() {}
        @Override @Test @Ignore("Not supported")
        public void test_execution_hosts() {}
    }
}