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

    // test cases
    public static class U6JobDescriptionTest extends JobDescriptionTest {
        public U6JobDescriptionTest() throws Exception {super("u6");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class U6JobRunMinimalTest extends JobRunMinimalTest {
        public U6JobRunMinimalTest() throws Exception {super("u6");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class U6JobRunRequiredTest extends JobRunRequiredTest {
        public U6JobRunRequiredTest() throws Exception {super("u6");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class U6JobRunOptionalTest extends JobRunOptionalTest {
        public U6JobRunOptionalTest() throws Exception {super("u6");}
        public void test_resume_done() { System.out.println("the test_resume_done ignored : the adaptor does not support this"); };
        public void test_resume_running() { System.out.println("the test_resume_running ignored : the adaptor does not support this"); };
        public void test_suspend_done() { System.out.println("the test_suspend_done ignored : the adaptor does not support this"); };
        public void test_suspend_running() { System.out.println("the test_resume_running ignored : the adaptor does not support this"); };
        public void test_listJob() { System.out.println("the test_listJob ignored : the adaptor does not support this but MUST BE REACTIVATED when the jsaga-engine will support this"); };
        public void test_multiJob() { System.out.println("the test_resume_running ignored : the adaptor does not support this"); };
        public void test_multiJobService() { System.out.println("the test_resume_running ignored : the adaptor does not support this"); };
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
 	// test cases
    public static class U6JobRunDescriptionTest extends JobRunDescriptionTest {
        public U6JobRunDescriptionTest() throws Exception {super("u6");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public U6ExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(U6JobDescriptionTest.class);
        this.addTestSuite(U6JobRunMinimalTest.class);
        this.addTestSuite(U6JobRunRequiredTest.class);
        this.addTestSuite(U6JobRunOptionalTest.class);
        this.addTestSuite(U6JobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new U6ExecutionTestSuite();
    }
}