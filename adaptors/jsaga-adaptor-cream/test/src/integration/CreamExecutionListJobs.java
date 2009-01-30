package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobListTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionListJobs
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionListJobs extends TestSuite {
    // test cases
    public static class CreamJobListTest extends JobListTest {
        public CreamJobListTest() throws Exception {super("cream");}
    }

    public CreamExecutionListJobs() throws Exception {
        super();
        // test cases
        this.addTestSuite(CreamJobListTest.class);
    }

    public static Test suite() throws Exception {
        return new CreamExecutionListJobs();
    }
}
