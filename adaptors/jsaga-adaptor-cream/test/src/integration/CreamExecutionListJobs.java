package integration;

import junit.framework.Test;
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
public class CreamExecutionListJobs extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new CreamExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(CreamExecutionTestSuite.class);}}

    /** test cases */
    public static class CreamJobListTest extends JobListTest {
        public CreamJobListTest() throws Exception {super("cream");}
    }
}
