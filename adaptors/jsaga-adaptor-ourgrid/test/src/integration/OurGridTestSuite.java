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
    public static class OurGridJobDescriptionTest extends JobDescriptionTest {
        public OurGridJobDescriptionTest() throws Exception {super("ourgrid");}
     }
    
    public static class OurGridJobRunMinimalTest extends JobRunMinimalTest {
        public OurGridJobRunMinimalTest() throws Exception {super("ourgrid");}
    }

    // test cases
    public static class OurGridJobRunRequiredTest extends JobRunRequiredTest {
        public OurGridJobRunRequiredTest() throws Exception {super("ourgrid");}
        // FIXME: server sould send FAILED
        public void test_run_error() { super.ignore("disabled because server sends RUNNING when command does not exists"); }
    }
    
    // test cases
    public static class OurGridJobRunDescriptionTest extends JobRunDescriptionTest {
        public OurGridJobRunDescriptionTest() throws Exception {super("ourgrid");}
        // FIXME: FAILED state is not handled properly
        public void test_run_inWorkingDirectory() { super.ignore("FAILED state is not handled properly"); }
        // FIXME: requirements not handled
        public void test_run_queueRequirement() throws Exception { super.ignore("Queue is ignored");}
        public void test_run_cpuTimeRequirement() throws Exception { super.ignore("TotalCPUTime is ignored");}
    }

      /** test cases */
    public static class OurGridJobRunSandboxTest extends JobRunSandboxTest {
        public OurGridJobRunSandboxTest() throws Exception {super("ourgrid");}
        // FIXME: stays RUNNING forever
        public void test_remote_input_explicit() throws Exception { super.ignore("DISABLED: stay RUNNING"); }
        // FIXME: stays RUNNING forever
        public void test_remote_output_explicit() throws Exception { super.ignore("DISABLED: stay RUNNING"); }
        // FIXME: stays RUNNING forever
        public void test_input_output_explicit() throws Exception { super.ignore("DISABLED: stay RUNNING"); }
        // FIXME: stays RUNNING forever
        public void test_input_output_implicit() throws Exception { super.ignore("DISABLED: stay RUNNING"); }
    
    }
    // test cases
    public static class OurGridJobRunInfoTest extends JobRunInfoTest {
    	public OurGridJobRunInfoTest() throws Exception {super("ourgrid");}
    	public void test_exitcode() throws Exception { super.ignore("Not supported"); }
    	public void test_created() throws Exception { super.ignore("Not supported"); }
    	public void test_dates() throws Exception { super.ignore("Not supported"); }
    }
    
}