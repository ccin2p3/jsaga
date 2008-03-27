package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

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
public class WMSExecutionTestSuite extends TestSuite {

    // test cases
    public static class WMSJobDescriptionTest extends JobDescriptionTest {
        public WMSJobDescriptionTest() throws Exception {super("wms");}
        public void test_totalCPUCount() { System.out.println("the test_totalCPUCount ignored : JDL does not support this"); }
        public void test_fileTransfer() { System.out.println("the test_fileTransfer ignored : JDL does not support this"); }
        public void test_cleanup() { System.out.println("the test_cleanup ignored : JDL does not support this"); }
        public void test_workingDirectory() { System.out.println("the test_cleanup ignored : JDL does not support this"); }
    }
    // test cases
    public static class WMSJobRunMinimalTest extends JobRunMinimalTest {
        public WMSJobRunMinimalTest() throws Exception {super("wms");}
    }
    
    // test cases
    public static class WMSJobRunRequiredTest extends JobRunRequiredTest {
        public WMSJobRunRequiredTest() throws Exception {super("wms");}
    }
    
    // test cases
    public static class WMSJobRunOptionalTest extends JobRunOptionalTest {
        public WMSJobRunOptionalTest() throws Exception {super("wms");}
        public void test_resume_done() { System.out.println("the test_resume_done ignored : personal gatekeeper does not support this"); };
        public void test_resume_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
        public void test_suspend_done() { System.out.println("the test_suspend_done ignored : personal gatekeeper does not support this"); };
        public void test_suspend_running() { System.out.println("the test_resume_running ignored : personal gatekeeper does not support this"); };
        public void test_listJob() { System.out.println("the test_listJob ignored : personal gatekeeper does not support this but MUST BE REACTIVATED when the jsaga-engine will support this"); };
    }
    
 	// test cases
    public static class WMSJobRunDescriptionTest extends JobRunDescriptionTest {
        public WMSJobRunDescriptionTest() throws Exception {super("wms");}
        //public void test_run_inWorkingDirectory() { System.out.println("the test_run_inWorkingDirectory ignored : wms does not support this"); };        
    }

    public WMSExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(WMSJobDescriptionTest.class);
        this.addTestSuite(WMSJobRunMinimalTest.class);
        this.addTestSuite(WMSJobRunRequiredTest.class);
        this.addTestSuite(WMSJobRunOptionalTest.class);
        this.addTestSuite(WMSJobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new WMSExecutionTestSuite();
    }
}