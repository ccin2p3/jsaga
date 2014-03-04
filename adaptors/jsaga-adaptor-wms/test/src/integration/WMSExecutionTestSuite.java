package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.job.ListTest;
import org.ogf.saga.job.base.JobBaseTest;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InfoTest;
import org.ogf.saga.job.run.InteractiveTest;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
import org.ogf.saga.job.run.RequirementsTest;
import org.ogf.saga.job.run.SandboxTest;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramExecutionTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    WMSExecutionTestSuite.WMSJobDescriptionTest.class,
    WMSExecutionTestSuite.WMSJobRunRequiredTest.class,
    WMSExecutionTestSuite.WMSJobRunOptionalTest.class,
    WMSExecutionTestSuite.WMSJobRunSandboxTest.class,
    WMSExecutionTestSuite.WMSJobRunInteractiveTest.class,
    WMSExecutionTestSuite.WMSJobRunDescriptionTest.class,
    WMSExecutionTestSuite.WMSJobRunInfoTest.class
})
public class WMSExecutionTestSuite  {

    public static class WMSJDLTest extends JobBaseTest {
        public WMSJDLTest() throws Exception {super("wms");}
        
        @Test
        public void test_jdl() throws SagaException {
            JobDescription desc = JobFactory.createJobDescription();
            desc.setAttribute(JobDescription.EXECUTABLE, "myScript.sh");
            desc.setAttribute(JobDescription.INPUT, "stdin.txt");
            desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
            desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myScript.sh > myScript.sh"});
            JobService service = JobFactory.createJobService(m_session, m_jobservice);
            Job job = service.createJob(desc);
            System.out.println(job.getAttribute("NativeJobDescription"));
        }
    }
    // test cases
    public static class WMSJobDescriptionTest extends DescriptionTest {
        public WMSJobDescriptionTest() throws Exception {super("wms");}
        @Override @Test @Ignore("Not supported")
        public void test_totalCPUCount() { }
        @Override @Test @Ignore("Not implemented")
        public void test_fileTransfer() { }
        @Override @Test @Ignore("Not supported")
        public void test_cleanup() { }
        @Override @Test @Ignore("Not supported")
        public void test_workingDirectory() { }        
        @Override @Test @Ignore("Not supported")
        public void test_threadsPerProcess() { }
        @Override @Test @Ignore("NumberOfProcesses only set with MPI")
        public void test_numberOfProcesses() { }
        @Override @Test @Ignore("Not supported")
        public void test_input() {  }
        @Override @Test @Ignore("Not supported")
        public void test_output() {  }
        @Override @Test @Ignore("Not supported")
        public void test_error() {  }
        @Override @Test @Ignore("Not supported")
        public void test_wallTimeLimit() {  }
    }
    // test cases
    public static class WMSJobRunMinimalTest extends MinimalTest {
        public WMSJobRunMinimalTest() throws Exception {super("wms");}
    }
    
    // test cases
    public static class WMSJobRunOptionalTest extends OptionalTest {
        public WMSJobRunOptionalTest() throws Exception {super("wms");}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {  }
        @Override @Test @Ignore("Not supported")
        public void test_listJob() { }
    }
    
    // test cases
    public static class WMSJobRunRequiredTest extends RequiredTest {
        public WMSJobRunRequiredTest() throws Exception {super("wms");}
    }
    
    // test cases
    public static class WMSJobRunSandboxTest extends SandboxTest {
        public WMSJobRunSandboxTest() throws Exception {super("wms");}
    }

    public static class WMSJobRunInteractiveTest extends InteractiveTest {
        public WMSJobRunInteractiveTest() throws Exception {super("wms");}
        @Override @Test @Ignore("Not supported")
        public void test_run_environnement() { }
    }
    
 	// test cases
    public static class WMSJobRunDescriptionTest extends RequirementsTest {
        public WMSJobRunDescriptionTest() throws Exception {super("wms");}
        @Override @Test @Ignore("Not supported")
       public void test_run_cpuTimeRequirement() {  }
        @Override @Test @Ignore("Not supported")
        public void test_run_inWorkingDirectory() {  }
    }

    // test cases
    public static class WMSJobRunInfoTest extends InfoTest {
    	public WMSJobRunInfoTest() throws Exception {super("wms");}
    }
    
    /** test cases */
    public static class WMSJobListTest extends ListTest {
        public WMSJobListTest() throws Exception {super("wms");}
    }

}