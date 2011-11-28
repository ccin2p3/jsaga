package integration;

import junit.framework.Test;

import org.ogf.saga.job.JobDescriptionTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunInfoTest;
import org.ogf.saga.job.JobRunInteractiveTest;
import org.ogf.saga.job.JobRunMinimalTest;
import org.ogf.saga.job.JobRunSandboxTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new CreamExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(CreamExecutionTestSuite.class);}}

    /** test cases */
    public static class CreamJobRunMinimalTest extends JobRunMinimalTest {
        public CreamJobRunMinimalTest() throws Exception {super("cream");}
    }

    public static class CreamJobDescriptionTest extends JobDescriptionTest {
        public CreamJobDescriptionTest() throws Exception {super("cream");}
        public void test_totalCPUCount() throws Exception {super.ignore("Not supported"); }
        public void test_workingDirectory() throws Exception {super.ignore("Not supported"); }
        public void test_threadsPerProcess() throws Exception {super.ignore("Not supported"); }
        public void test_cleanup() throws Exception {super.ignore("Not supported"); }
        public void test_candidateHosts() throws Exception {super.ignore("Not supported"); }
        public void test_queue() throws Exception {super.ignore("SAGA queue is override by Queue in URL"); }
     }

    public static class CreamJobRunDescriptionTest extends JobRunDescriptionTest {
        public CreamJobRunDescriptionTest() throws Exception {super("cream");}
        public void test_run_inWorkingDirectory() { super.ignore("WorkingDirectory is not supported"); }
        public void test_run_queueRequirement() throws Exception {super.ignore("SAGA queue is override by Queue in URL"); }
        //public void test_run_cpuTimeRequirement() throws Exception { super.ignore("TotalCPUTime is ignored");}
    }

    public static class CreamJobRunSandboxTest extends JobRunSandboxTest {
        public CreamJobRunSandboxTest() throws Exception {super("cream");}
    }
    
    public static class CreamJobRunInteractiveTest extends JobRunInteractiveTest {
    	public CreamJobRunInteractiveTest() throws Exception {super("cream");}
    	public void test_run_environnement() throws Exception { super.ignore("Space is not supported in environment");   }
    }

    public static class CreamJobRunInfoTest extends JobRunInfoTest {
    	public CreamJobRunInfoTest() throws Exception {super("cream");}
    }
    
}
