package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
***************************************************
* Description:                                      */
/**
 *
 */
public class BesGenesisIITestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BesGenesisIITestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BesGenesisIITestSuite.class);}}

    // test cases
    public static class BesGenesisIIJobDescriptionTest extends JobDescriptionTest {
        public BesGenesisIIJobDescriptionTest() throws Exception {super("bes-genesis2");}
     }
    
    // test cases
    public static class BesGenesisIIJobRunMinimalTest extends JobRunMinimalTest {
        public BesGenesisIIJobRunMinimalTest() throws Exception {super("bes-genesis2");}
    }
    
    // test cases
    public static class BesGenesisIIJobRunRequiredTest extends JobRunRequiredTest {
        public BesGenesisIIJobRunRequiredTest() throws Exception {super("bes-genesis2");}
        public void test_run_error() { super.ignore("return code not supported"); }
    }
    
    // test cases
    public static class BesGenesisIIJobRunOptionalTest extends JobRunOptionalTest {
        public BesGenesisIIJobRunOptionalTest() throws Exception {super("bes-genesis2");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }
    
 	// test cases
    //public static class BesGenesisIIJobRunSandboxTest extends JobRunSandboxTest {
    //    public BesGenesisIIJobRunSandboxTest() throws Exception {super("bes-genesis2");}
    //}
    
 	// test cases
    public static class BesGenesisIIJobRunDescriptionTest extends JobRunDescriptionTest {
        public BesGenesisIIJobRunDescriptionTest() throws Exception {super("bes-genesis2");}
        public void test_run_inWorkingDirectory() { super.ignore("return code not supported"); }
        public void test_run_queueRequirement() { super.ignore("not supported"); }
        public void test_run_cpuTimeRequirement() { super.ignore("not supported"); }
    }

    // test cases
    //public static class BesGenesisIIJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public BesGenesisIIJobRunInteractiveTest() throws Exception {super("bes-genesis2");}
    //}
}