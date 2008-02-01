package integration;

import org.ogf.saga.job.JobRunBasicTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunOptionalTest;

import junit.framework.Test;
import junit.framework.TestSuite;

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
public class GlobusExecutionTestSuiteForWindows extends TestSuite {
    
	// test cases
    public static class GlobusJobRunBasicTest extends JobRunBasicTest {
        public GlobusJobRunBasicTest() throws Exception {super("gatekeeper-windows");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
        public void test_run_error() { System.out.println("the test_run_error ignored : personal gatekeeper does not support this"); };
    }
    
    // test cases
    public static class GlobusJobRunOptionalTest extends JobRunOptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super("gatekeeper-windows");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
        public void test_resume_done() { System.out.println("the test_resume_done ignored : personal gatekeeper does not support this"); };
        public void test_resume_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
        public void test_suspend_done() { System.out.println("the test_suspend_done ignored : personal gatekeeper does not support this"); };
        public void test_suspend_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends JobRunDescriptionTest {
        public GlobusJobRunDescriptionTest() throws Exception {super("gatekeeper-windows");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public GlobusExecutionTestSuiteForWindows() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunBasicTest.class);
        this.addTestSuite(GlobusJobRunOptionalTest.class);
        this.addTestSuite(GlobusJobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuiteForWindows();
    }
}