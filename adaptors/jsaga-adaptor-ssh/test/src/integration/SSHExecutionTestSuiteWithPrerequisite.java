package integration;

import org.ogf.saga.job.JobRunWithPrequisiteTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHExecutionTestSuiteWithPrerequisite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/

public class SSHExecutionTestSuiteWithPrerequisite extends TestSuite {
    // test cases
    public static class SSHJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public SSHJobRunWithPrequisiteTest() throws Exception {super("ssh");}         
        public void test_run_MPI() { super.ignore("not supported"); }
     }

    public SSHExecutionTestSuiteWithPrerequisite() throws Exception {
        super();
        // test cases
        this.addTestSuite(SSHJobRunWithPrequisiteTest.class);
    }

    public static Test suite() throws Exception {
        return new SSHExecutionTestSuiteWithPrerequisite();
    }
}