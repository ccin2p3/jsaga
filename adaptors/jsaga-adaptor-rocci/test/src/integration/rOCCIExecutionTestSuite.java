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
* File:   rOCCIExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
****************************************************/

@RunWith(Suite.class)
@SuiteClasses({
    rOCCIExecutionTestSuite.rOCCIJobDescriptionTest.class,
    rOCCIExecutionTestSuite.rOCCIJobRunRequiredTest.class,
    rOCCIExecutionTestSuite.rOCCIJobRunOptionalTest.class,
    rOCCIExecutionTestSuite.rOCCIJobRunSandboxTest.class,
    rOCCIExecutionTestSuite.rOCCIJobRunInteractiveTest.class,
    rOCCIExecutionTestSuite.rOCCIJobRunInfoTest.class
})
public class rOCCIExecutionTestSuite {
    
    private final static String TYPE = "rocci";

    // test cases
    public static class rOCCIJobDescriptionTest extends DescriptionTest {
        public rOCCIJobDescriptionTest() throws Exception {super(TYPE);}
        public void test_spmdVariation() {  }
        public void test_totalCPUCount() {  }
        public void test_numberOfProcesses() {  }
        public void test_processesPerHost() {  }
        public void test_threadsPerProcess() {  }
//        public void test_input() {  }
//        public void test_fileTransfer() {  }
        public void test_cleanup() {  }
        public void test_totalCPUTime() {  }
        public void test_totalPhysicalMemory() {  }
        public void test_cpuArchitecture() {  }
        public void test_operatingSystemType() {  }
        public void test_candidateHosts() {  }
        public void test_queue() {  }
        public void test_wallTimeLimit() {  }
     }
    
    // test cases
    public static class rOCCIJobRunMinimalTest extends MinimalTest {
        public rOCCIJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class rOCCIJobRunRequiredTest extends RequiredTest {
        public rOCCIJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class rOCCIJobRunSandboxTest extends SandboxTest {
        public rOCCIJobRunSandboxTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_output_workingDirectory() {  }
    }
    
    // test cases
    public static class rOCCIJobRunOptionalTest extends OptionalTest {
        public rOCCIJobRunOptionalTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {  }
    }
    
     // test cases
//    public static class rOCCIJobRunDescriptionTest extends RequirementsTest {
//        public rOCCIJobRunDescriptionTest() throws Exception {super(TYPE);}
//        public void test_run_queueRequirement() {  }
//        public void test_run_cpuTimeRequirement() {  }
//        public void test_run_memoryRequirement() {  }
//    }
    
    // test cases
    public static class rOCCIJobRunInteractiveTest extends InteractiveTest {
        public rOCCIJobRunInteractiveTest() throws Exception {super(TYPE);}
        @Override @Test @Ignore("Not supported")
        public void test_simultaneousStdin()  { }
    }

    // test cases
    public static class rOCCIJobRunInfoTest extends InfoTest {
        public rOCCIJobRunInfoTest() throws Exception {super(TYPE);}
    }
}