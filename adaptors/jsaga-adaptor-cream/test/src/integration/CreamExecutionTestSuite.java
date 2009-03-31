package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new CreamExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(CreamExecutionTestSuite.class);}}

    /** test cases */
    public static class CreamJobRunMinimalTest extends JobRunMinimalTest {
        public CreamJobRunMinimalTest() throws Exception {super("cream");}
    }
}
