package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalExecutionTestSuiteMinimal
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   9=29 avril 2008
****************************************************/

public class LocalExecutionTestSuiteMinimal extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LocalExecutionTestSuiteMinimal();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LocalExecutionTestSuiteMinimal.class);}}

    // test cases
    public static class LocalJobRunMinimalTest extends JobRunMinimalTest {
        public LocalJobRunMinimalTest() throws Exception {super("local");}
    }
}