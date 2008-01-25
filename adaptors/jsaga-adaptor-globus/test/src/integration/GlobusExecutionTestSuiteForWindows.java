package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunTest;

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
public class GlobusExecutionTestSuiteForWindows extends TestSuite {
    // test cases
    public static class GlobusJobRunTest extends JobRunTest {
        public GlobusJobRunTest() throws Exception {super("gatekeeper-test");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public GlobusExecutionTestSuiteForWindows() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuiteForWindows();
    }
}