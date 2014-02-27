package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.*;
import org.ogf.saga.job.abstracts.Attribute;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InteractiveTest;
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
* File:   GlobusExecutionTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    GlobusExecutionTestSuite.GlobusJobRunRequiredTest.class,
    GlobusExecutionTestSuite.GlobusJobRunOptionalTest.class,
    GlobusExecutionTestSuite.GlobusJobDescriptionTest.class,
    GlobusExecutionTestSuite.GlobusJobRunDescriptionTest.class,
    GlobusExecutionTestSuite.GlobusJobRunSandboxTest.class,
    GlobusExecutionTestSuite.GlobusJobRunInteractiveTest.class
    })
public class GlobusExecutionTestSuite {

    protected static String TYPE = "gatekeeper";

    // test cases
    public static class GlobusJobDescriptionTest extends DescriptionTest {
        public GlobusJobDescriptionTest() throws Exception {super(TYPE);}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_totalCPUCount() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_threadsPerProcess() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_fileTransfer() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_cleanup() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_cpuArchitecture() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_operatingSystemType() { }
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_candidateHosts() {}
        @Test @Ignore("RSL v1.0 does not support this")
        public void test_wallTimeLimit() {}
    }

    // test cases
    public static class GlobusJobRunMinimalTest extends MinimalTest {
        public GlobusJobRunMinimalTest() throws Exception {super(TYPE);}
     }
    
    // test cases
    public static class GlobusJobRunRequiredTest extends RequiredTest {
        public GlobusJobRunRequiredTest() throws Exception {super(TYPE);}
    }
    
    // test cases
    public static class GlobusJobRunSandboxTest extends SandboxTest {
        public GlobusJobRunSandboxTest() throws Exception {super(TYPE);}
    }

    // test cases
    public static class GlobusJobRunOptionalTest extends OptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super(TYPE);}
        @Test @Ignore("Not supported")
        public void test_resume_done() {}
        @Test @Ignore("Not supported")
        public void test_resume_running() {}
        @Test @Ignore("Not supported")
        public void test_suspend_done() {}
        @Test @Ignore("Not supported")
        public void test_suspend_running() {}
        @Test @Ignore("not supported by adaptor but MUST BE REACTIVATED when supported by the engine")
        public void test_listJob() {}
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends RequirementsTest {
        public GlobusJobRunDescriptionTest() throws Exception {super(TYPE);}
        @Test @Override @Ignore("Unexpected error: The job manager failed to open stdout")
        public void test_run_inWorkingDirectory() { } 

        @Test
        public void test_run_MPI() throws Exception {
            
            // prepare a job start a mpi binary
            Attribute[] attributes = new Attribute[3];
            attributes[0] = new Attribute(JobDescription.SPMDVARIATION, "MPI");
            attributes[1] = new Attribute(JobDescription.NUMBEROFPROCESSES, "2");
            attributes[2] = new Attribute(JobDescription.PROCESSESPERHOST, "2");
            JobDescription desc =  createJob("helloMpi", attributes, null);
            
            // submit
            Job job = runJob(desc);
            
            // wait for the end
            job.waitFor();  
            
            // check job status
            assertEquals(
                    State.DONE,
                    job.getState());       
        }
}

    public static class GlobusJobRunInteractiveTest extends InteractiveTest {
        public GlobusJobRunInteractiveTest() throws Exception {super(TYPE);}
    }

}