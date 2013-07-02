package integration;

import junit.framework.Test;

import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobDescriptionTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunInfoTest;
import org.ogf.saga.job.JobRunInteractiveTest;
import org.ogf.saga.job.JobRunMinimalTest;
import org.ogf.saga.job.JobRunOptionalTest;
import org.ogf.saga.job.JobRunRequiredTest;
import org.ogf.saga.job.JobRunSandboxTest;
import org.ogf.saga.task.State;

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

    // test cases
    public static class CreamJobRunRequiredTest extends JobRunRequiredTest {
        public CreamJobRunRequiredTest() throws Exception {super("cream");}
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
    }

    public static class CreamJobRunOptionalTest extends JobRunOptionalTest {
        public CreamJobRunOptionalTest() throws Exception {super("cream");}
    	/**
         * Runs a long job, waits for running state and suspends it
         */
        public void test_suspend_queued() throws Exception {
            
        	// prepare
        	JobDescription desc = createLongJob();
        	
            // submit
            Job job = runJob(desc);
            
            // wait for RUNNING state (deviation from SAGA specification)
            if (! super.waitForSubState(job, MODEL+":PENDING")) {
            	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
                fail("Job did not enter PENDING state within "+MAX_QUEUING_TIME+" seconds");
            }
            
            try {
            	
    	        // suspend job
    	        job.suspend();
    	        
    	        // wait for 1 second because suspend is an asynchronous method
    	        job.waitFor(1);
    	        
    	        // check job status
    	        assertEquals(
    	                State.SUSPENDED,
    	                job.getState());

    	        // resume job
    	        job.resume();
    	        
    	        // wait for 1 second
    	        job.waitFor(1);
    	        
    	        // check job status
    	        assertEquals(
    	                State.RUNNING,
    	                job.getState());
            }
            finally {
            	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
            }
        }
        

    }

    public static class CreamJobRunSandboxTest extends JobRunSandboxTest {
        public CreamJobRunSandboxTest() throws Exception {super("cream");}
        public void test_output_workingDirectory() throws Exception { super.ignore("WorkingDirectory is not supported");   }
    }
    
    public static class CreamJobRunInteractiveTest extends JobRunInteractiveTest {
    	public CreamJobRunInteractiveTest() throws Exception {super("cream");}
    	public void test_run_environnement() throws Exception { super.ignore("Space is not supported in environment");   }
    }

    public static class CreamJobRunInfoTest extends JobRunInfoTest {
    	public CreamJobRunInfoTest() throws Exception {super("cream");}
    }
    
}
