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
public class ArexTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new ArexTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(ArexTestSuite.class);}}

    // test cases
    public static class ArexJobDescriptionTest extends JobDescriptionTest {
        public ArexJobDescriptionTest() throws Exception {super("arex");}
     }
    
    // test cases
    public static class ArexJobRunMinimalTest extends JobRunMinimalTest {
        public ArexJobRunMinimalTest() throws Exception {super("arex");}
    }
    
    // test cases
    public static class ArexJobRunRequiredTest extends JobRunRequiredTest {
        public ArexJobRunRequiredTest() throws Exception {super("arex");}
        public void test_run_error() { super.ignore("return code not supported"); }
    }
    
    // test cases
    public static class ArexJobRunOptionalTest extends JobRunOptionalTest {
        public ArexJobRunOptionalTest() throws Exception {super("arex");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }

    // test cases
    public static class ArexJobRunDescriptionTest extends JobRunDescriptionTest {
        public ArexJobRunDescriptionTest() throws Exception {super("arex");}
        //public void test_run_inWorkingDirectory() { super.ignore("return code not supported"); }
        //public void test_run_queueRequirement() { super.ignore("not supported"); }
        //public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
    }

 	// test cases
    // TODO : implement HTTPS PUT
    public static class ArexJobRunSandboxTest extends JobRunSandboxTest {
        public ArexJobRunSandboxTest() throws Exception {super("arex");}
        public void test_remote_input_explicit() { super.ignore("HTTPS PUT not supported"); }
        public void test_remote_output_explicit() { super.ignore("HTTPS PUT not supported"); }
        public void test_input_output_explicit() { super.ignore("HTTPS PUT not supported"); }
        public void test_input_output_implicit() { super.ignore("HTTPS PUT not supported"); }
    }
    
    // test cases
    public static class ArexJobRunInteractiveTest extends JobRunInteractiveTest {
        public ArexJobRunInteractiveTest() throws Exception {super("arex");}
    }
}