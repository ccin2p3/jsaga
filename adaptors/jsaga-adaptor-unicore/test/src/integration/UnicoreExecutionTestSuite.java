package integration;

import java.io.InputStream;

import junit.framework.Test;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.*;
import org.ogf.saga.job.abstracts.Attribute;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   02/09/2011
***************************************************
* Description:                                      */
/**
 *
 */
public class UnicoreExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new UnicoreExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(UnicoreExecutionTestSuite.class);}}

    // test cases
    public static class UnicoreJobDescriptionTest extends JobDescriptionTest {
        public UnicoreJobDescriptionTest() throws Exception {super("unicore");}
        public void test_fileTransfer() {super.ignore("unicore");}
        public void test_cleanup() {super.ignore("unicore");}
     }
    
    // test cases
    public static class UnicoreJobRunMinimalTest extends JobRunMinimalTest {
        public UnicoreJobRunMinimalTest() throws Exception {super("unicore");}
    }
    
    // test cases
    public static class UnicoreJobRunRequiredTest extends JobRunRequiredTest {
        public UnicoreJobRunRequiredTest() throws Exception {super("unicore");}
    }
    
    // test cases
    public static class UnicoreJobRunOptionalTest extends JobRunOptionalTest {
        public UnicoreJobRunOptionalTest() throws Exception {super("unicore");}
        public void test_suspend_running() { super.ignore("not supported"); }
    }
    
 	// test cases
    public static class UnicoreJobRunSandboxTest extends JobRunSandboxTest {
        public UnicoreJobRunSandboxTest() throws Exception {super("unicore");}
    }
    
 	// test cases
    public static class UnicoreJobRunDescriptionTest extends JobRunDescriptionTest {
        public UnicoreJobRunDescriptionTest() throws Exception {super("unicore");}
        public void test_run_inWorkingDirectory() { super.ignore("not supported"); }
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
        public void test_run_MPI() throws Exception {
            
        	Attribute[] attributes = new Attribute[3];
        	attributes[0] = new Attribute(JobDescription.SPMDVARIATION, "OpenMPI");
        	attributes[1] = new Attribute(JobDescription.NUMBEROFPROCESSES, "4");
        	attributes[2] = new Attribute(JobDescription.PROCESSESPERHOST, "2");
        	JobDescription desc =  createJob(SIMPLE_JOB_BINARY, attributes, null);
        	
        	// submit
            Job job = runJob(desc);
            
            // wait for the end
            job.waitFor();  
            
            // check job status
            assertEquals(
                    State.DONE,
                    job.getState());       
        }
            
        public void test_run_unsupportedApplication() throws Exception {
            
        	Attribute[] attributes = new Attribute[1];
        	attributes[0] = new Attribute(JobDescription.JOBPROJECT, "Unsupported application");
        	JobDescription desc =  createJob(SIMPLE_JOB_BINARY, attributes, null);
        	
        	// submit
        	try {
        		Job job = runJob(desc);
        		fail("Expected exception");
        	} catch (NoSuccessException nse) {
        	}
            
        }

        public void test_run_dateApplication() throws Exception {
            
            JobDescription desc = JobFactory.createJobDescription();
            desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
            desc.setAttribute(JobDescription.INTERACTIVE, "True");
            desc.setAttribute(JobDescription.JOBPROJECT, "Date");

        	// submit
            Job job = runJob(desc);
            
            // wait for the end
            job.waitFor();  
            
            // check job status
            assertEquals(
                    State.DONE,
                    job.getState());       
            
            byte[] buffer = new byte[1024];
            InputStream stdout = job.getStdout();
            for (int len; (len=stdout.read(buffer))>-1; ) {
                System.err.write(buffer, 0, len);
            }
            stdout.close();
            assertFalse(new String(buffer).equals(""));
        }

    }

    public static class UnicoreJobRunInfoTest extends JobRunInfoTest {
    	public UnicoreJobRunInfoTest() throws Exception {super("unicore");}
    	public void test_dates() {super.ignore("getStarted is not supported"); }
    	public void test_execution_hosts() {super.ignore("not supported"); }
    }
    
    // test cases
    //public static class UnicoreJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public UnicoreJobRunInteractiveTest() throws Exception {super("unicore");}
    //}
}