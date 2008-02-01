package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ogf.saga.job.JobRunBasicTest;
import org.ogf.saga.job.JobRunDescriptionTest;
import org.ogf.saga.job.JobRunOptionalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusExecutionTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusExecutionTestSuite extends TestSuite {
	
    // test cases
    public static class GlobusJobRunBasicTest extends JobRunBasicTest {
        public GlobusJobRunBasicTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    // test cases
    public static class GlobusJobRunOptionalTest extends JobRunOptionalTest {
        public GlobusJobRunOptionalTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
 	// test cases
    public static class GlobusJobRunDescriptionTest extends JobRunDescriptionTest {
        public GlobusJobRunDescriptionTest() throws Exception {super("gatekeeper");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public GlobusExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunBasicTest.class);
        this.addTestSuite(GlobusJobRunOptionalTest.class);
        this.addTestSuite(GlobusJobRunDescriptionTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuite();
    }
}