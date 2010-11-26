package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesExecutionFirstTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 nov 2010
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BesExecutionTestSuiteMinimal extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BesExecutionTestSuiteMinimal();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BesExecutionTestSuiteMinimal.class);}}

    /** test cases */
    public static class BesJobRunMinimalTest extends JobRunMinimalTest {
        public BesJobRunMinimalTest() throws Exception {super("bes");}
    }
}