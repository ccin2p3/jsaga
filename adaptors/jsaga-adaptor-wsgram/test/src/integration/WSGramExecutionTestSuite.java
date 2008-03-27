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
public class WSGramExecutionTestSuite extends TestSuite {

    // test cases
    public static class WSGramJobDescriptionTest extends JobDescriptionTest {
        public WSGramJobDescriptionTest() throws Exception {super("wsgram");}
        public void test_totalCPUCount() { System.out.println("the test_totalCPUCount ignored : RSL v1.0 does not support this"); }
        public void test_threadsPerProcess() { System.out.println("the test_threadsPerProcess ignored : RSL v1.0 does not support this"); }
        public void test_fileTransfer() { System.out.println("the test_fileTransfer ignored : RSL v1.0 does not support this"); }
        public void test_cleanup() { System.out.println("the test_cleanup ignored : RSL v1.0 does not support this"); }
        public void test_cpuArchitecture() { System.out.println("the test_cpuArchitecture ignored : RSL v1.0 does not support this"); }
        public void test_operatingSystemType() { System.out.println("the test_operatingSystemType ignored : RSL v1.0 does not support this"); }
        public void test_candidateHosts() { System.out.println("the test_candidateHosts ignored : RSL v1.0 does not support this"); }
    }

    // test cases
    public static class WSGramJobRunMinimalTest extends JobRunMinimalTest {
        public WSGramJobRunMinimalTest() throws Exception {super("wsgram");}
    }
    
    // test cases
    public static class WSGramJobRunRequiredTest extends JobRunRequiredTest {
        public WSGramJobRunRequiredTest() throws Exception {super("wsgram");}
     }
    
    // test cases
    public static class WSGramJobRunOptionalTest extends JobRunOptionalTest {
        public WSGramJobRunOptionalTest() throws Exception {super("wsgram");}
        public void test_resume_done() { System.out.println("the test_resume_done ignored : wsgram does not support this"); };
        public void test_resume_running() { System.out.println("the test_resume_running ignored : wsgram does not support this"); };
        public void test_suspend_done() { System.out.println("the test_suspend_done ignored : wsgram does not support this"); };
        public void test_suspend_running() { System.out.println("the test_resume_running ignored : wsgram does not support this"); };
        public void test_listJob() { System.out.println("the test_listJob ignored : wsgram does not support this but MUST BE REACTIVATED when the jsaga-engine will support this"); };
    }
    
 	// test cases
    public static class WSGramJobRunDescriptionTest extends JobRunDescriptionTest {
        public WSGramJobRunDescriptionTest() throws Exception {super("wsgram");}
    }

    public WSGramExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(WSGramJobDescriptionTest.class);
        this.addTestSuite(WSGramJobRunMinimalTest.class);
        this.addTestSuite(WSGramJobRunRequiredTest.class);
        this.addTestSuite(WSGramJobRunOptionalTest.class);
        this.addTestSuite(WSGramJobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new WSGramExecutionTestSuite();
    }
}