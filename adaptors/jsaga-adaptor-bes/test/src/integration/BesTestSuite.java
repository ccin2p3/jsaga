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
public class BesTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BesTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BesTestSuite.class);}}

    // test cases
    public static class BesJobDescriptionTest extends JobDescriptionTest {
        public BesJobDescriptionTest() throws Exception {super("bes");}
     }
    
    // test cases
    public static class BesJobRunMinimalTest extends JobRunMinimalTest {
        public BesJobRunMinimalTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BesJobRunRequiredTest extends JobRunRequiredTest {
        public BesJobRunRequiredTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BesJobRunOptionalTest extends JobRunOptionalTest {
        public BesJobRunOptionalTest() throws Exception {super("bes");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported but MUST BE REACTIVATED when the jsaga-engine will support this"); }
    }
    
 	// test cases
    public static class BesJobRunDescriptionTest extends JobRunDescriptionTest {
        public BesJobRunDescriptionTest() throws Exception {super("bes");}
        public void test_run_queueRequirement() { super.ignore("not supported"); }
    }

    // test cases
    public static class BesJobRunInteractiveTest extends JobRunInteractiveTest {
        public BesJobRunInteractiveTest() throws Exception {super("bes");}
    }
}