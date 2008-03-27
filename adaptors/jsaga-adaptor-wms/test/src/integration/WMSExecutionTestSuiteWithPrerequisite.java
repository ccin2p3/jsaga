package integration;

import org.ogf.saga.job.JobRunWithPrequisiteTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramExecutionTestSuiteWithPrerequisite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WMSExecutionTestSuiteWithPrerequisite extends TestSuite {
    // test cases
    public static class WMSJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public WMSJobRunWithPrequisiteTest() throws Exception {super("wms");}
        public void test_run_MPI() { System.out.println("the test_run_MPI ignored : jsaga-engine must support input sandbox"); };        
    }

    public WMSExecutionTestSuiteWithPrerequisite() throws Exception {
        super();
        // test cases
        this.addTestSuite(WMSJobRunWithPrequisiteTest.class);
    }

    public static Test suite() throws Exception {
        return new WMSExecutionTestSuiteWithPrerequisite();
    }
}