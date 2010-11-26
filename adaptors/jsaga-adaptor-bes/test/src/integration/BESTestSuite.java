package integration;

import junit.framework.Test;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BESTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
***************************************************
* Description:                                      */
/**
 *
 */
public class BESTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BESTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BESTestSuite.class);}}

    // test cases
    public static class BESJobDescriptionTest extends JobDescriptionTest {
        public BESJobDescriptionTest() throws Exception {super("bes");}
     }
    
    // test cases
    public static class BESJobRunMinimalTest extends JobRunMinimalTest {
        public BESJobRunMinimalTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BESJobRunRequiredTest extends JobRunRequiredTest {
        public BESJobRunRequiredTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BESJobRunOptionalTest extends JobRunOptionalTest {
        public BESJobRunOptionalTest() throws Exception {super("bes");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported but MUST BE REACTIVATED when the jsaga-engine will support this"); }
    }
    
 	// test cases
    public static class BESJobRunDescriptionTest extends JobRunDescriptionTest {
        public BESJobRunDescriptionTest() throws Exception {super("bes");}
        public void test_run_queueRequirement() { super.ignore("not supported"); }
    }

    // test cases
    public static class BESJobRunInteractiveTest extends JobRunInteractiveTest {
        public BESJobRunInteractiveTest() throws Exception {super("bes");}
    }
}