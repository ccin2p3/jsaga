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
        public void test_totalCPUCount() { super.ignore("JDL does not support this"); }
        public void test_totalCPUTime() { super.ignore("JDL does not support this"); }
        public void test_fileTransfer() { super.ignore("not yet implemented but MUST BE REACTIVATED when implemented"); }
        public void test_cleanup() { super.ignore("JDL does not support this"); }
        public void test_workingDirectory() { super.ignore("JDL does not support this"); }        
        public void test_threadsPerProcess() { super.ignore("JDL does not support this"); }
        public void test_operatingSystemType() { super.ignore("JDL does not support this"); }
        public void test_cpuArchitecture() { super.ignore("JDL does not support this"); }
        public void test_numberOfProcesses() { super.ignore("NumberOfProcesses only set with MPI"); }
        public void test_input() { super.ignore("not supported"); }
        public void test_output() { super.ignore("not supported"); }
        public void test_error() { super.ignore("not supported"); }
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
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported by adaptor but MUST BE REACTIVATED when supported by the engine"); }
    }
    
 	// test cases
    public static class WMSJobRunDescriptionTest extends JobRunDescriptionTest {
        public WMSJobRunDescriptionTest() throws Exception {super("wms");}
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
        public void test_run_inWorkingDirectory() { super.ignore("not supported"); }
        public void test_run_processRequirement() { super.ignore("not supported"); }
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