package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusExecutionTestSuiteInteractive extends TestSuite {

    // test cases
    public static class GlobusJobRunInteractiveTest extends JobRunInteractiveTest {
        public GlobusJobRunInteractiveTest() throws Exception {super("gatekeeper");}
    }
    
    public GlobusExecutionTestSuiteInteractive() throws Exception {
        super();
        // test cases
        this.addTestSuite(GlobusJobRunInteractiveTest.class);
    }

    public static Test suite() throws Exception {
        return new GlobusExecutionTestSuiteInteractive();
    }
}