package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ogf.saga.job.JobDescriptionTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunInteractiveTest;
import org.ogf.saga.job.JobRunMinimalTest;
import org.ogf.saga.job.JobRunOptionalTest;
import org.ogf.saga.job.JobRunRequiredTest;
import org.ogf.saga.job.JobRunSandboxTest;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BatchSSHJobAdaptorTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BatchSSHJobAdaptorTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BatchSSHJobAdaptorTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BatchSSHJobAdaptorTestSuite.class);}}

    // test cases
    public static class BatchSSHJobDescriptionTest extends JobDescriptionTest {
        public BatchSSHJobDescriptionTest() throws Exception {super("pbs-ssh");}
        public void test_spmdVariation() { super.ignore("not supported"); }
        //public void test_totalCPUCount() { super.ignore("not supported"); }
        public void test_numberOfProcesses() { super.ignore("not supported"); }
        public void test_processesPerHost() { super.ignore("not supported"); }
        public void test_threadsPerProcess() { super.ignore("not supported"); }
        public void test_input() { super.ignore("not supported"); }
        //public void test_output() { super.ignore("not supported"); }
        //public void test_error() { super.ignore("not supported"); }
        public void test_fileTransfer() { super.ignore("not supported"); }
        public void test_cleanup() { super.ignore("not supported"); }
        public void test_totalCPUTime() { super.ignore("not supported"); }
        //public void test_totalPhysicalMemory() { super.ignore("not supported"); }
        public void test_cpuArchitecture() { super.ignore("not supported"); }
        public void test_operatingSystemType() { super.ignore("not supported"); }
        public void test_candidateHosts() { super.ignore("not supported"); }
        public void test_queue() { super.ignore("not supported"); }
     }
    
    // test cases
    public static class BatchSSHJobRunMinimalTest extends JobRunMinimalTest {
        public BatchSSHJobRunMinimalTest() throws Exception {super("pbs-ssh");}
    }
    
    // test cases
    public static class BatchSSHJobRunRequiredTest extends JobRunRequiredTest {
        public BatchSSHJobRunRequiredTest() throws Exception {super("pbs-ssh");}
    }

    // test cases
    //public static class BatchSSHJobRunSandboxTest extends JobRunSandboxTest {
    //    public BatchSSHJobRunSandboxTest() throws Exception {super("pbs-ssh");}
    //}
    
    // test cases
    public static class BatchSSHJobRunOptionalTest extends JobRunOptionalTest {
        public BatchSSHJobRunOptionalTest() throws Exception {super("pbs-ssh");}
        //public void test_resume_done() { super.ignore("not supported"); }
        //public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_simultaneousShortJob() throws Exception { super.ignore("not working");}
        public void test_simultaneousLongJob() throws Exception { super.ignore("not working");}
    }
    
 	// test cases
    public static class BatchSSHJobRunDescriptionTest extends JobRunDescriptionTest {
        public BatchSSHJobRunDescriptionTest() throws Exception {super("pbs-ssh");}
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
        public void test_run_memoryRequirement() { super.ignore("not supported"); }
    }
    
    // test cases
    //public static class BatchSSHJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public BatchSSHJobRunInteractiveTest() throws Exception {super("pbs-ssh");}
    //    public void test_simultaneousStdin()  { super.ignore("Not supported");}
    //}
}
