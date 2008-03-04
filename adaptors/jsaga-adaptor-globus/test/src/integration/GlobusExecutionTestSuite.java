package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
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
public class GlobusExecutionTestSuite extends TestSuite {

    // test cases
    public static class GlobusJobDescriptionTest extends JobDescriptionTest {
        public GlobusJobDescriptionTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
        public void test_totalCPUCount() { System.out.println("the test_totalCPUCount ignored : RSL v1.0 does not support this"); }
        public void test_threadsPerProcess() { System.out.println("the test_threadsPerProcess ignored : RSL v1.0 does not support this"); }
        public void test_fileTransfer() { System.out.println("the test_fileTransfer ignored : RSL v1.0 does not support this"); }
        public void test_cleanup() { System.out.println("the test_cleanup ignored : RSL v1.0 does not support this"); }
        public void test_cpuArchitecture() { System.out.println("the test_cpuArchitecture ignored : RSL v1.0 does not support this"); }
        public void test_operatingSystemType() { System.out.println("the test_operatingSystemType ignored : RSL v1.0 does not support this"); }
        public void test_candidateHosts() { System.out.println("the test_candidateHosts ignored : RSL v1.0 does not support this"); }
    }

    // test cases
    public static class GlobusJobRunMinimalTest extends JobRunMinimalTest {
        public GlobusJobRunMinimalTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class GlobusJobRunRequiredTest extends JobRunRequiredTest {
        public GlobusJobRunRequiredTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class GlobusJobRunOptionalTest extends JobRunOptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super("gatekeeper");}
        public void test_resume_done() { System.out.println("the test_resume_done ignored : personal gatekeeper does not support this"); };
        public void test_resume_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
        public void test_suspend_done() { System.out.println("the test_suspend_done ignored : personal gatekeeper does not support this"); };
        public void test_suspend_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
        public void test_listJob() { System.out.println("the test_listJob ignored : personal gatekeeper does not support this but MUST BE REACTIVATED when the jsaga-engine will support this"); };
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends JobRunDescriptionTest {
        public GlobusJobRunDescriptionTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public GlobusExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunMinimalTest.class);
        this.addTestSuite(GlobusJobDescriptionTest.class);
        this.addTestSuite(GlobusJobRunRequiredTest.class);
        this.addTestSuite(GlobusJobRunOptionalTest.class);
        this.addTestSuite(GlobusJobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuite();
    }
}