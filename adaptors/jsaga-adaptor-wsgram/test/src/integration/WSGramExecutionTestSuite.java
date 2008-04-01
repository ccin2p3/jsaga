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
        public void test_totalCPUCount() { super.ignore("RSL v2.0 does not support this"); }
        public void test_threadsPerProcess() { super.ignore("RSL v2.0 does not support this"); }
        public void test_fileTransfer() { super.ignore("RSL v2.0 does not support this"); }
        public void test_cleanup() { super.ignore("RSL v2.0 does not support this"); }
        public void test_cpuArchitecture() { super.ignore("RSL v2.0 does not support this"); }
        public void test_operatingSystemType() { super.ignore("RSL v2.0 does not support this"); }
        public void test_candidateHosts() { super.ignore("RSL v2.0 does not support this"); }
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
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported by adaptor but MUST BE REACTIVATED when supported by the engine"); }
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