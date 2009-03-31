package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunWithPrequisiteTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHExecutionTestSuiteWithPrerequisite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/

public class SSHExecutionTestSuiteWithPrerequisite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new SSHExecutionTestSuiteWithPrerequisite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(SSHExecutionTestSuiteWithPrerequisite.class);}}

    /** test cases */
    public static class SSHJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public SSHJobRunWithPrequisiteTest() throws Exception {super("ssh");}         
        public void test_run_MPI() { super.ignore("not supported"); }
     }
}