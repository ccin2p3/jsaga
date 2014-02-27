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
import org.ogf.saga.job.run.RequirementsTest;
import org.ogf.saga.job.run.SandboxTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 NOV 2013
****************************************************/

@RunWith(Suite.class)
@SuiteClasses({
	SSHExecutionTestSuite.SSHJobDescriptionTest.class, 
	SSHExecutionTestSuite.SSHJobRunRequiredTest.class,
	SSHExecutionTestSuite.SSHJobRunOptionalTest.class,
	SSHExecutionTestSuite.SSHJobRunSandboxTest.class,
	SSHExecutionTestSuite.SSHJobRunDescriptionTest.class,
	SSHExecutionTestSuite.SSHJobRunInteractiveTest.class,
	SSHExecutionTestSuite.SSHJobRunInfoTest.class})
public class SSHExecutionTestSuite {
	
	private final static String TYPE = "ssh";

    // test cases
    public static class SSHJobDescriptionTest extends DescriptionTest {
        public SSHJobDescriptionTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not supported")
        public void test_spmdVariation() { }
//        public void test_totalCPUCount() { super.ignore("not supported"); }
//        public void test_numberOfProcesses() { super.ignore("not supported"); }
//        public void test_processesPerHost() { super.ignore("not supported"); }
//        public void test_threadsPerProcess() { super.ignore("not supported"); }
//        public void test_input() { super.ignore("not supported"); }
//        public void test_output() { super.ignore("not supported"); }
//        public void test_error() { super.ignore("not supported"); }
//        public void test_fileTransfer() { super.ignore("not supported"); }
//        public void test_cleanup() { super.ignore("not supported"); }
//        public void test_totalCPUTime() { super.ignore("not supported"); }
//        public void test_totalPhysicalMemory() { super.ignore("not supported"); }
//        public void test_cpuArchitecture() { super.ignore("not supported"); }
//        public void test_operatingSystemType() { super.ignore("not supported"); }
//        public void test_candidateHosts() { super.ignore("not supported"); }
//        public void test_queue() { super.ignore("not supported"); }
//        public void test_wallTimeLimit() { super.ignore("not supported"); }
     }
    
    // test cases
    public static class SSHJobRunMinimalTest extends MinimalTest {
        public SSHJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class SSHJobRunRequiredTest extends RequiredTest {
        public SSHJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class SSHJobRunSandboxTest extends SandboxTest {
        public SSHJobRunSandboxTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class SSHJobRunOptionalTest extends OptionalTest {
        public SSHJobRunOptionalTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not working: Exception in thread \"Timer-0\"")
        public void test_simultaneousLongJob() { }

        @Test @Ignore("Not working: Exception in thread \"Timer-0\"")
        public void test_simultaneousShortJob() { }
        
        @Test @Ignore
        public void test_resume_done() {}
        
        @Test @Ignore
        public void test_suspend_done() {}
        
        @Test @Ignore
        public void test_suspend_running() {}
    }
    
 	// test cases
    public static class SSHJobRunDescriptionTest extends RequirementsTest {
        public SSHJobRunDescriptionTest() throws Exception {super(TYPE);}
        @Test @Override @Ignore
        public void test_run_queueRequirement() {}

        @Test @Override @Ignore
        public void test_run_cpuTimeRequirement() {}
    }
    
    // test cases
    public static class SSHJobRunInteractiveTest extends InteractiveTest {
        public SSHJobRunInteractiveTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class SSHJobRunInfoTest extends InfoTest {
        public SSHJobRunInfoTest() throws Exception {super(TYPE);}
    }
}