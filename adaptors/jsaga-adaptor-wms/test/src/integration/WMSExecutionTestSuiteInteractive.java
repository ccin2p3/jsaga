package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

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
public class WMSExecutionTestSuiteInteractive extends TestSuite {

    // test cases
    public static class WMSJobRunInteractiveTest extends JobRunInteractiveTest {
        public WMSJobRunInteractiveTest() throws Exception {super("wms");}
        public void test_getStderr() { super.ignore("JDL does not support this"); }
        public void test_run_environnement() { super.ignore("JDL does not support this"); }
    }
    
    public WMSExecutionTestSuiteInteractive() throws Exception {
        super();
        // test cases
        this.addTestSuite(WMSJobRunInteractiveTest.class);
    }

    public static Test suite() throws Exception {
        return new WMSExecutionTestSuiteInteractive();
    }
}