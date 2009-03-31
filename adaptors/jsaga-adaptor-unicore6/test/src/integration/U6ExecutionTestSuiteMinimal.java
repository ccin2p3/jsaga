package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6ExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U6ExecutionTestSuiteMinimal extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new U6ExecutionTestSuiteMinimal();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(U6ExecutionTestSuiteMinimal.class);}}

    /** test cases */
    public static class U6JobRunMinimalTest extends JobRunMinimalTest {
        public U6JobRunMinimalTest() throws Exception {super("unicore6");}
    }
}