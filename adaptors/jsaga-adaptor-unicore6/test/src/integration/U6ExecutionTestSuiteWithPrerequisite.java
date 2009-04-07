package integration;

import junit.framework.Test;
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
public class U6ExecutionTestSuiteWithPrerequisite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new U6ExecutionTestSuiteWithPrerequisite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(U6ExecutionTestSuiteWithPrerequisite.class);}}

    /** test cases */
    public static class U6JobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public U6JobRunWithPrequisiteTest() throws Exception {super("unicore6");}
     }
}