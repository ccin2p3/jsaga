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
public class WSGramExecutionTestSuiteWithPrerequisite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new WSGramExecutionTestSuiteWithPrerequisite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(WSGramExecutionTestSuiteWithPrerequisite.class);}}

    /** test cases */
    public static class WSGramJobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public WSGramJobRunWithPrequisiteTest() throws Exception {super("wsgram");}
    }
}