package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunWithPrequisiteTest;

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
    /** create test suite */
    public static Test suite() throws Exception {return new GlobusExecutionTestSuiteWithPrerequisite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(GlobusExecutionTestSuiteWithPrerequisite.class);}}

    // test cases
    public static class GlobusJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public GlobusJobRunWithPrequisiteTest() throws Exception {super("gatekeeper");}
    }
}