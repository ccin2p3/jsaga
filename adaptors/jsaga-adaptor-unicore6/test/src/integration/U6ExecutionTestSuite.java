package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6ExecutionTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
***************************************************
* Description:                                      */
/**
 *
 */
public class U6ExecutionTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new U6ExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(U6ExecutionTestSuite.class);}}

    // test cases
    public static class U6JobDescriptionTest extends JobDescriptionTest {
        public U6JobDescriptionTest() throws Exception {super("unicore6");}
     }
    
    // test cases
    public static class U6JobRunMinimalTest extends JobRunMinimalTest {
        public U6JobRunMinimalTest() throws Exception {super("unicore6");}
    }
    
    // test cases
    public static class U6JobRunRequiredTest extends JobRunRequiredTest {
        public U6JobRunRequiredTest() throws Exception {super("unicore6");}
    }
    
    // test cases
    public static class U6JobRunOptionalTest extends JobRunOptionalTest {
        public U6JobRunOptionalTest() throws Exception {super("unicore6");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
        public void test_listJob() { super.ignore("not supported but MUST BE REACTIVATED when the jsaga-engine will support this"); }
    }
    
 	// test cases
    public static class U6JobRunDescriptionTest extends JobRunDescriptionTest {
        public U6JobRunDescriptionTest() throws Exception {super("unicore6");}
        public void test_run_queueRequirement() { super.ignore("not supported"); }
    }

    // test cases
    public static class U6JobRunInteractiveTest extends JobRunInteractiveTest {
        public U6JobRunInteractiveTest() throws Exception {super("unicore6");}
    }
}