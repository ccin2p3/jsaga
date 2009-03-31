package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunWithPrequisiteTest;

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
    /** create test suite */
    public static Test suite() throws Exception {return new WMSExecutionTestSuiteWithPrerequisite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(WMSExecutionTestSuiteWithPrerequisite.class);}}

    /** test cases */
    public static class WMSJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public WMSJobRunWithPrequisiteTest() throws Exception {super("wms");}
        public void test_run_MPI() { super.ignore("jsaga-engine must support input sandbox"); };        
    }
}