package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

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
public class GlobusExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new GlobusExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(GlobusExecutionTestSuite.class);}}

    // test cases
    public static class GlobusJobDescriptionTest extends JobDescriptionTest {
        public GlobusJobDescriptionTest() throws Exception {super("gatekeeper");}
        public void test_totalCPUCount() { super.ignore("RSL v1.0 does not support this"); }
        public void test_threadsPerProcess() { super.ignore("RSL v1.0 does not support this"); }
        public void test_fileTransfer() { super.ignore("RSL v1.0 does not support this"); }
        public void test_cleanup() { super.ignore("RSL v1.0 does not support this"); }
        public void test_cpuArchitecture() { super.ignore("RSL v1.0 does not support this"); }
        public void test_operatingSystemType() { super.ignore("RSL v1.0 does not support this"); }
        public void test_candidateHosts() { super.ignore("RSL v1.0 does not support this"); }
        public void test_wallTimeLimit() { super.ignore("RSL v1.0 does not support this"); }
    }

    // test cases
    public static class GlobusJobRunMinimalTest extends JobRunMinimalTest {
        public GlobusJobRunMinimalTest() throws Exception {super("gatekeeper");}
     }
    
    // test cases
    public static class GlobusJobRunRequiredTest extends JobRunRequiredTest {
        public GlobusJobRunRequiredTest() throws Exception {super("gatekeeper");}
    }
    
    // test cases
    public static class GlobusJobRunOptionalTest extends JobRunOptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super("gatekeeper");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported by adaptor but MUST BE REACTIVATED when supported by the engine"); }
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends JobRunDescriptionTest {
        public GlobusJobRunDescriptionTest() throws Exception {super("gatekeeper");}
        public void test_run_inWorkingDirectory() { super.ignore("Unexpected error: The job manager failed to open stdout"); } 
    }
}