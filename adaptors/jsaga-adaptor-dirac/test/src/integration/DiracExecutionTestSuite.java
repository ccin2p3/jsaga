package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
****************************************************/

public class DiracExecutionTestSuite extends JSAGATestSuite {
	
	private final static String TYPE = "dirac";

    /** create test suite */
    public static Test suite() throws Exception {return new DiracExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(DiracExecutionTestSuite.class);}}

    // test cases
    public static class DiracJobDescriptionTest extends JobDescriptionTest {
        public DiracJobDescriptionTest() throws Exception {super(TYPE);}
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
    public static class DiracJobRunMinimalTest extends JobRunMinimalTest {
        public DiracJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class DiracJobRunRequiredTest extends JobRunRequiredTest {
        public DiracJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class DiracJobRunSandboxTest extends JobRunSandboxTest {
        public DiracJobRunSandboxTest() throws Exception {super(TYPE);}
        public void test_output_workingDirectory() { super.ignore("not supported"); }
    }
    
    // test cases
    public static class DiracJobRunOptionalTest extends JobRunOptionalTest {
        public DiracJobRunOptionalTest() throws Exception {super(TYPE);}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
//        public void test_simultaneousShortJob() throws Exception { super.ignore("not working");}
//        public void test_simultaneousLongJob() throws Exception { super.ignore("not working");}
    }
    
 	// test cases
//    public static class DiracJobRunDescriptionTest extends JobRunDescriptionTest {
//        public DiracJobRunDescriptionTest() throws Exception {super(TYPE);}
//        public void test_run_queueRequirement() { super.ignore("not supported"); }
//        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
//        public void test_run_memoryRequirement() { super.ignore("not supported"); }
//    }
    
    // test cases
    public static class DiracJobRunInteractiveTest extends JobRunInteractiveTest {
        public DiracJobRunInteractiveTest() throws Exception {super(TYPE);}
        public void test_simultaneousStdin()  { super.ignore("Not supported");}
    }

    // test cases
    public static class DiracJobRunInfoTest extends JobRunInfoTest {
        public DiracJobRunInfoTest() throws Exception {super(TYPE);}
        public void test_exitcode() {super.ignore("Not supported");}
        public void test_execution_hosts() {super.ignore("Not supported");}
    }
}