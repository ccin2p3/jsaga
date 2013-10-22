package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   rOCCIExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
****************************************************/

public class rOCCIExecutionTestSuite extends JSAGATestSuite {
	
	private final static String TYPE = "rocci";

    /** create test suite */
    public static Test suite() throws Exception {return new rOCCIExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(rOCCIExecutionTestSuite.class);}}

    // test cases
    public static class rOCCIJobDescriptionTest extends JobDescriptionTest {
        public rOCCIJobDescriptionTest() throws Exception {super(TYPE);}
        public void test_spmdVariation() { super.ignore("not supported"); }
        public void test_totalCPUCount() { super.ignore("not supported"); }
        public void test_numberOfProcesses() { super.ignore("not supported"); }
        public void test_processesPerHost() { super.ignore("not supported"); }
        public void test_threadsPerProcess() { super.ignore("not supported"); }
//        public void test_input() { super.ignore("not supported"); }
//        public void test_fileTransfer() { super.ignore("not supported"); }
        public void test_cleanup() { super.ignore("not supported"); }
        public void test_totalCPUTime() { super.ignore("not supported"); }
        public void test_totalPhysicalMemory() { super.ignore("not supported"); }
        public void test_cpuArchitecture() { super.ignore("not supported"); }
        public void test_operatingSystemType() { super.ignore("not supported"); }
        public void test_candidateHosts() { super.ignore("not supported"); }
        public void test_queue() { super.ignore("not supported"); }
        public void test_wallTimeLimit() { super.ignore("not supported"); }
     }
    
    // test cases
    public static class rOCCIJobRunMinimalTest extends JobRunMinimalTest {
        public rOCCIJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class rOCCIJobRunRequiredTest extends JobRunRequiredTest {
        public rOCCIJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class rOCCIJobRunSandboxTest extends JobRunSandboxTest {
        public rOCCIJobRunSandboxTest() throws Exception {super(TYPE);}
        public void test_output_workingDirectory() { super.ignore("not supported"); }
    }
    
    // test cases
    public static class rOCCIJobRunOptionalTest extends JobRunOptionalTest {
        public rOCCIJobRunOptionalTest() throws Exception {super(TYPE);}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }
    
 	// test cases
//    public static class rOCCIJobRunDescriptionTest extends JobRunDescriptionTest {
//        public rOCCIJobRunDescriptionTest() throws Exception {super(TYPE);}
//        public void test_run_queueRequirement() { super.ignore("not supported"); }
//        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
//        public void test_run_memoryRequirement() { super.ignore("not supported"); }
//    }
    
    // test cases
    public static class rOCCIJobRunInteractiveTest extends JobRunInteractiveTest {
        public rOCCIJobRunInteractiveTest() throws Exception {super(TYPE);}
        public void test_simultaneousStdin()  { super.ignore("Not supported");}
    }

    // test cases
    public static class rOCCIJobRunInfoTest extends JobRunInfoTest {
        public rOCCIJobRunInfoTest() throws Exception {super(TYPE);}
    }
}