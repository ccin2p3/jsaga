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
    // test cases
    public static class CreamJobRunMinimalTest extends JobRunMinimalTest {
        public CreamJobRunMinimalTest() throws Exception {super("cream");}
    }

    public CreamExecutionTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(CreamJobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new CreamExecutionTestSuite();
    }
}
