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
public class WSGramExecutionTestSuiteWithPrerequisite extends TestSuite {
    // test cases
    public static class WSGramJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public WSGramJobRunWithPrequisiteTest() throws Exception {super("wsgram");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public WSGramExecutionTestSuiteWithPrerequisite() throws Exception {
        super();
        // test cases
        this.addTestSuite(WSGramJobRunWithPrequisiteTest.class);
    }

    public static Test suite() throws Exception {
        return new WSGramExecutionTestSuiteWithPrerequisite();
    }
}