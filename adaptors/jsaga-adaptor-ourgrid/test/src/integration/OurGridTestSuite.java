package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ogf.saga.job.JobDescriptionTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunInfoTest;
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
* File:   MyProtocolIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class OurGridTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new OurGridTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(OurGridTestSuite.class);}}

    // test cases
    public static class OurgridJobDescriptionTest extends JobDescriptionTest {
        public OurgridJobDescriptionTest() throws Exception {super("ourgrid");}
     }
    
    public static class OurGridJobRunMinimalTest extends JobRunMinimalTest {
        public OurGridJobRunMinimalTest() throws Exception {super("ourgrid");}
    }

    // test cases
    public static class OurgridJobRunRequiredTest extends JobRunRequiredTest {
        public OurgridJobRunRequiredTest() throws Exception {super("ourgrid");}
//        public void test_run_error() { super.ignore("return code not supported"); }
    }
    
    // test cases
    public static class OurgridJobRunOptionalTest extends JobRunOptionalTest {
        public OurgridJobRunOptionalTest() throws Exception {super("ourgrid");}
//        public void test_resume_done() { super.ignore("not supported"); }
//        public void test_resume_running() { super.ignore("not supported"); }
//        public void test_suspend_done() { super.ignore("not supported"); }
//        public void test_suspend_running() { super.ignore("not supported"); }
    }

    // test cases
    public static class OurgridJobRunDescriptionTest extends JobRunDescriptionTest {
        public OurgridJobRunDescriptionTest() throws Exception {super("ourgrid");}
        //public void test_run_inWorkingDirectory() { super.ignore("WorkingDirectory is not supported"); }
//        public void test_run_cpuTimeRequirement() throws Exception { super.ignore("TotalCPUTime is ignored");}
    }

      /** test cases */
    public static class OurGridJobRunSandboxTest extends JobRunSandboxTest {
        public OurGridJobRunSandboxTest() throws Exception {super("ourgrid");}
//        public void test_remote_input_explicit() throws Exception { super.ignore("Not supported yet."); }
//        public void test_remote_output_explicit() throws Exception { super.ignore("Not supported yet."); }
//        public void test_input_output_explicit() throws Exception { super.ignore("Not supported yet."); }
//        public void test_input_output_implicit() throws Exception { super.ignore("Not supported yet."); }
//        public void test_output_only_implicit() throws Exception { super.ignore("Not supported yet."); }
    
    }
    // test cases
//    public static class OurgridJobRunInfoTest extends JobRunInfoTest {
//    	public OurgridJobRunInfoTest() throws Exception {super("ourgrid");}
//    }
//    
//    public static class OurgridJobRunInteractiveTest extends JobRunInteractiveTest {
//    	public OurgridJobRunInteractiveTest() throws Exception {super("ourgrid");}
//    }

    
}