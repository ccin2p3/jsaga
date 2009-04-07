package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WSGramExecutionTestSuiteMinimal extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new WSGramExecutionTestSuiteMinimal();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(WSGramExecutionTestSuiteMinimal.class);}}

    /** test cases */
    public static class WSGramJobRunMinimalTest extends JobRunMinimalTest {
        public WSGramJobRunMinimalTest() throws Exception {super("wsgram");}
   }
}