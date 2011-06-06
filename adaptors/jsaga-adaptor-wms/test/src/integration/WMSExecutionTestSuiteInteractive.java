package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunInteractiveTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSExecutionTestSuiteInteractive
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   28 avril 2008
***************************************************
* Description:                                      */
/**
 *
 */
public class WMSExecutionTestSuiteInteractive extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new WMSExecutionTestSuiteInteractive();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(WMSExecutionTestSuiteInteractive.class);}}

    /** test cases */
    public static class WMSJobRunInteractiveTest extends JobRunInteractiveTest {
        public WMSJobRunInteractiveTest() throws Exception {super("wms");}
        public void test_run_environnement() { super.ignore("JDL does not support space in environment value"); }
    }
}