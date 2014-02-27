package integration;

import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.abstracts.Attribute;
import org.ogf.saga.job.abstracts.AttributeVector;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InfoTest;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
import org.ogf.saga.job.run.RequirementsTest;
import org.ogf.saga.job.run.SandboxTest;
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
@RunWith(Suite.class)
@SuiteClasses({
    UnicoreExecutionTestSuite.UnicoreJobDescriptionTest.class,
    UnicoreExecutionTestSuite.UnicoreJobRunOptionalTest.class,
    UnicoreExecutionTestSuite.UnicoreJobRunRequiredTest.class,
    UnicoreExecutionTestSuite.UnicoreJobRunSandboxTest.class,
    UnicoreExecutionTestSuite.UnicoreJobRunDescriptionTest.class,
    UnicoreExecutionTestSuite.UnicoreJobRunInfoTest.class
})
public class UnicoreExecutionTestSuite {

    // test cases
    public static class UnicoreJobDescriptionTest extends DescriptionTest {
        public UnicoreJobDescriptionTest() throws Exception {super("unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_fileTransfer() {}
        @Override @Test @Ignore("Not supported")
        public void test_cleanup() {}
     }
    
    // test cases
    public static class UnicoreJobRunMinimalTest extends MinimalTest {
        public UnicoreJobRunMinimalTest() throws Exception {super("unicore");}
    }
    
    // test cases
    public static class UnicoreJobRunRequiredTest extends RequiredTest {
        public UnicoreJobRunRequiredTest() throws Exception {super("unicore");}
    }
    
    // test cases
    public static class UnicoreJobRunOptionalTest extends OptionalTest {
        public UnicoreJobRunOptionalTest() throws Exception {super("unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {  }
    }
    
     // test cases
    public static class UnicoreJobRunSandboxTest extends SandboxTest {
        public UnicoreJobRunSandboxTest() throws Exception {super("unicore");}
    }
    
     // test cases
    public static class UnicoreJobRunDescriptionTest extends RequirementsTest {
        public UnicoreJobRunDescriptionTest() throws Exception {super("unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_run_inWorkingDirectory() {  }
        @Override @Test @Ignore("Not supported")
        public void test_run_queueRequirement() {  }
        @Override @Test @Ignore("Not supported")
        public void test_run_cpuTimeRequirement() {  }

        @Test
        public void test_run_MPI() throws Exception {
            
            Attribute[] attributes = new Attribute[5];
            attributes[0] = new Attribute(JobDescription.SPMDVARIATION, "OpenMPI");
            attributes[1] = new Attribute(JobDescription.NUMBEROFPROCESSES, "4");
            attributes[2] = new Attribute(JobDescription.PROCESSESPERHOST, "2");
            attributes[3] = new Attribute(JobDescription.TOTALCPUCOUNT, "4");
            attributes[4] = new Attribute(JobDescription.THREADSPERPROCESS, "3");
            AttributeVector[] vAttributes = new AttributeVector[1];
            vAttributes[0] = new AttributeVector("Extension", new String[]{"UserPreCommand=mpicc job.c"});
            JobDescription desc =  createJob(SIMPLE_JOB_BINARY, attributes, vAttributes);
            
            // submit
            Job job = runJob(desc);
            
            // wait for the end
            job.waitFor();  
            
            // check job status
            assertEquals(
                    State.DONE,
                    job.getState());       
        }
            
        @Test(expected=NoSuccessException.class)
        public void test_run_unsupportedApplication() throws Exception {
            
            Attribute[] attributes = new Attribute[1];
            attributes[0] = new Attribute(JobDescription.JOBPROJECT, "Unsupported application");
            JobDescription desc =  createJob(SIMPLE_JOB_BINARY, attributes, null);
            // submit
            Job job = runJob(desc);
        }

        @Test
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

    public static class UnicoreJobRunInfoTest extends InfoTest {
        public UnicoreJobRunInfoTest() throws Exception {super("unicore");}
        @Override @Test @Ignore("getStarted not supported")
        public void test_dates() { }
        @Override @Test @Ignore("Not supported")
        public void test_execution_hosts() { }
    }
    
}