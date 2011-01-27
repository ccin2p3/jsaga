package integration;

import junit.framework.Test;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.*;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
***************************************************
* Description:                                      */
/**
 *
 */
public class ArexTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new ArexTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(ArexTestSuite.class);}}

    // test cases
    public static class ArexJobDescriptionTest extends JobDescriptionTest {
        public ArexJobDescriptionTest() throws Exception {super("arex");}
     }
    
    // test cases
    public static class ArexJobRunMinimalTest extends JobRunMinimalTest {
        public ArexJobRunMinimalTest() throws Exception {super("arex");}
    }
    
    // test cases
    public static class ArexJobRunRequiredTest extends JobRunRequiredTest {
        public ArexJobRunRequiredTest() throws Exception {super("arex");}
        public void test_run_error() { super.ignore("return code not supported"); }
    }
    
    // test cases
    public static class ArexJobRunOptionalTest extends JobRunOptionalTest {
        public ArexJobRunOptionalTest() throws Exception {super("arex");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }

    // test cases
    public static class ArexJobRunDescriptionTest extends JobRunDescriptionTest {
        public ArexJobRunDescriptionTest() throws Exception {super("arex");}
        public void test_run_inWorkingDirectory() { super.ignore("WorkingDirectory is not supported"); }
        
        // FIXME: returned DONE instead of FAILED
        public void test_run_cpuTimeRequirement() { super.ignore("walltimelimit is ignored and job is stuck"); }
        /*
        public void test_run_cpuTimeRequirement() throws Exception {
            
        	// prepare a simple job
        	JobDescription desc = createSimpleJob();
        	// and inform the scheduler to the estimate time is 14 days
        	desc.setAttribute(JobDescription.WALLTIMELIMIT, String.valueOf(3001));
        	
        	Job job = null;
        	try {
        		// submit
    	        job = runJob(desc);
    	        
    	        // wait the end
    	        job.waitFor();  
    	        
    	        // check job status
    	        assertEquals(
    	                State.FAILED,
    	                job.getState());
        	}
        	catch (NoSuccessException noSuccess) {
            	// test is successful is exception instance of BadResource
                if (!noSuccess.getClass().getName().endsWith("BadResource")) {
                    throw noSuccess;
                }
            }
        	finally {
            	if(job != null) {
            		job.waitFor(Float.valueOf(FINALY_TIMEOUT));
            	}
            }
    	        
        }*/
    }

 	// test cases
    public static class ArexJobRunSandboxTest extends JobRunSandboxTest {
        public ArexJobRunSandboxTest() throws Exception {super("arex");}
    }
    
    // test cases
    /*public static class ArexJobRunInteractiveTest extends JobRunInteractiveTest {
        public ArexJobRunInteractiveTest() throws Exception {super("arex");}
    }*/
    
    // test cases
    public static class ArexJobRunInfoTest extends JobRunInfoTest {
    	public ArexJobRunInfoTest() throws Exception {super("arex");}
    	public void test_created() {super.ignore("Creation date not supported"); }
    }
}