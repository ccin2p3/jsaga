package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreExecutionTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   02/09/2011
***************************************************
* Description:                                      */
/**
 *
 */
public class UnicoreExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new UnicoreExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(UnicoreExecutionTestSuite.class);}}

    // test cases
    public static class UnicoreJobDescriptionTest extends JobDescriptionTest {
        public UnicoreJobDescriptionTest() throws Exception {super("unicore");}
        public void test_fileTransfer() {super.ignore("unicore");}
        public void test_cleanup() {super.ignore("unicore");}
     }
    
    // test cases
    public static class UnicoreJobRunMinimalTest extends JobRunMinimalTest {
        public UnicoreJobRunMinimalTest() throws Exception {super("unicore");}
    }
    
    // test cases
    public static class UnicoreJobRunRequiredTest extends JobRunRequiredTest {
        public UnicoreJobRunRequiredTest() throws Exception {super("unicore");}
        // TODO: uncomment when server bug is fixed
        public void test_run_long() { super.ignore("Bug in server: status FAILED for job sleep"); }
        public void test_cancel_running() { super.ignore("Bug in server: status FAILED for job sleep"); }
    }
    
    // test cases
    public static class UnicoreJobRunOptionalTest extends JobRunOptionalTest {
        public UnicoreJobRunOptionalTest() throws Exception {super("unicore");}
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("Bug in server: status FAILED for job sleep"); }
        public void test_simultaneousLongJob() { super.ignore("Bug in server: status FAILED for job sleep"); }
    }
    
 	// test cases
    public static class UnicoreJobRunSandboxTest extends JobRunSandboxTest {
        public UnicoreJobRunSandboxTest() throws Exception {super("unicore");}
    }
    
 	// test cases
    public static class UnicoreJobRunDescriptionTest extends JobRunDescriptionTest {
        public UnicoreJobRunDescriptionTest() throws Exception {super("unicore");}
        public void test_run_inWorkingDirectory() { super.ignore("not supported"); }
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
    }

    // test cases
    //public static class UnicoreJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public UnicoreJobRunInteractiveTest() throws Exception {super("unicore");}
    //}
}