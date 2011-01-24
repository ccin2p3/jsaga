package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
***************************************************
* Description:                                      */
/**
 *
 */
public class BesUnicoreTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BesUnicoreTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BesUnicoreTestSuite.class);}}

    // test cases
    public static class BesUnicoreJobDescriptionTest extends JobDescriptionTest {
        public BesUnicoreJobDescriptionTest() throws Exception {super("bes-unicore");}
     }
    
    // test cases
    public static class BesUnicoreJobRunMinimalTest extends JobRunMinimalTest {
        public BesUnicoreJobRunMinimalTest() throws Exception {super("bes-unicore");}
    }
    
    // test cases
    public static class BesUnicoreJobRunRequiredTest extends JobRunRequiredTest {
        public BesUnicoreJobRunRequiredTest() throws Exception {super("bes-unicore");}
        public void test_run_error() { super.ignore("return code not supported"); }
    }
    
    // test cases
    public static class BesUnicoreJobRunOptionalTest extends JobRunOptionalTest {
        public BesUnicoreJobRunOptionalTest() throws Exception {super("bes-unicore");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }
    
 	// test cases
    public static class BesUnicoreJobRunSandboxTest extends JobRunSandboxTest {
        public BesUnicoreJobRunSandboxTest() throws Exception {super("bes-unicore");}
    }
    
 	// test cases
    public static class BesUnicoreJobRunDescriptionTest extends JobRunDescriptionTest {
        public BesUnicoreJobRunDescriptionTest() throws Exception {super("bes-unicore");}
        public void test_run_inWorkingDirectory() { super.ignore("return code not supported"); }
        // TODO : test_run_queueRequirement
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        //public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
    }

    // test cases
    public static class BesUnicoreJobRunInteractiveTest extends JobRunInteractiveTest {
        public BesUnicoreJobRunInteractiveTest() throws Exception {super("bes-unicore");}
    }
}