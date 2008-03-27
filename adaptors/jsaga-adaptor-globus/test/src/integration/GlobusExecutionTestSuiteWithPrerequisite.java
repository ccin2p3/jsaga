package integration;

import org.ogf.saga.job.JobRunWithPrequisiteTest;

import junit.framework.Test;
import junit.framework.TestSuite;

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
public class GlobusExecutionTestSuiteWithPrerequisite extends TestSuite {
    // test cases
    public static class GlobusJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public GlobusJobRunWithPrequisiteTest() throws Exception {super("gatekeeper");}
    }

    public GlobusExecutionTestSuiteWithPrerequisite() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunWithPrequisiteTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuiteWithPrerequisite();
    }
}