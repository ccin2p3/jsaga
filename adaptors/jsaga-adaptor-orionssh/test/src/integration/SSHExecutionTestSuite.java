package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHExecutionTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/

public class SSHExecutionTestSuite extends JSAGATestSuite {
	
	private final static String TYPE = "ssh";

    /** create test suite */
    public static Test suite() throws Exception {return new SSHExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(SSHExecutionTestSuite.class);}}

    // test cases
    public static class SSHJobDescriptionTest extends JobDescriptionTest {
        public SSHJobDescriptionTest() throws Exception {super(TYPE);}
        public void test_spmdVariation() { super.ignore("not supported"); }
        public void test_totalCPUCount() { super.ignore("not supported"); }
        public void test_numberOfProcesses() { super.ignore("not supported"); }
        public void test_processesPerHost() { super.ignore("not supported"); }
        public void test_threadsPerProcess() { super.ignore("not supported"); }
        public void test_input() { super.ignore("not supported"); }
        public void test_output() { super.ignore("not supported"); }
        public void test_error() { super.ignore("not supported"); }
        public void test_fileTransfer() { super.ignore("not supported"); }
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
    public static class SSHJobRunMinimalTest extends JobRunMinimalTest {
        public SSHJobRunMinimalTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class SSHJobRunRequiredTest extends JobRunRequiredTest {
        public SSHJobRunRequiredTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class SSHJobRunSandboxTest extends JobRunSandboxTest {
        public SSHJobRunSandboxTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class SSHJobRunOptionalTest extends JobRunOptionalTest {
        public SSHJobRunOptionalTest() throws Exception {super(TYPE);}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
//        public void test_simultaneousShortJob() throws Exception { super.ignore("not working");}
//        public void test_simultaneousLongJob() throws Exception { super.ignore("not working");}
    }
    
 	// test cases
    public static class SSHJobRunDescriptionTest extends JobRunDescriptionTest {
        public SSHJobRunDescriptionTest() throws Exception {super(TYPE);}
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
        public void test_run_memoryRequirement() { super.ignore("not supported"); }
    }
    
    // test cases
    public static class SSHJobRunInteractiveTest extends JobRunInteractiveTest {
        public SSHJobRunInteractiveTest() throws Exception {super(TYPE);}
//        public void test_simultaneousStdin()  { super.ignore("Not supported");}
    }

    // test cases
    public static class SSHJobRunInfoTest extends JobRunInfoTest {
        public SSHJobRunInfoTest() throws Exception {super(TYPE);}
    }
}