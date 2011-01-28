package integration;

import junit.framework.Test;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.*;
import org.ogf.saga.job.abstracts.Attribute;
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
        
        public void test_run_cpuTimeRequirement() throws Exception {
        	super.ignore("TotalCPUTime is ignored");
        }
        
        public void test_run_wallTimeRequirement() throws Exception {
        	super.ignore("walltimelimit is ignored and job is stuck"); if (true) return;
        	// prepare a simple job
        	JobDescription desc = createSimpleJob();
        	// and inform the scheduler to the estimate time is 14 days
        	desc.setAttribute(JobDescription.WALLTIMELIMIT, String.valueOf(60*60*24*14));
        	
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
    	        
        }
        /**
         * Runs simple job, requests 4096 Mo of memory and expects FAILED status
         */
        public void test_run_memoryRequirement() throws Exception {
            
        	// prepare a job witch requires 8GB of RAM and expect FAILED status
        	Attribute[] attributes = new Attribute[1];
        	attributes[0] = new Attribute(JobDescription.TOTALPHYSICALMEMORY, "8192");
        	JobDescription desc =  createJob(SIMPLE_JOB_BINARY, attributes, null);
        	
        	// submit
            Job job = runJob(desc);
            
            // wait for the end
            job.waitFor();  
            
            // check job status
            assertEquals(
                    State.FAILED,
                    job.getState());       
        }
            
    }

 	// test cases
    public static class ArexJobRunSandboxTest extends JobRunSandboxTest {
        public ArexJobRunSandboxTest() throws Exception {super("arex");}
    }
    
    // test cases
    public static class ArexJobRunInfoTest extends JobRunInfoTest {
    	public ArexJobRunInfoTest() throws Exception {super("arex");}
    	public void test_created() {super.ignore("Creation date not supported"); }
    }
}