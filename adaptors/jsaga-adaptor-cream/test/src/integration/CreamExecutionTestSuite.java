package integration;

import junit.framework.Test;

import org.ogf.saga.job.JobRunInteractiveTest;
import org.ogf.saga.job.JobRunMinimalTest;
import org.ogf.saga.job.JobRunSandboxTest;

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
public class CreamExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new CreamExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(CreamExecutionTestSuite.class);}}

    /** test cases */
    public static class CreamJobRunMinimalTest extends JobRunMinimalTest {
        public CreamJobRunMinimalTest() throws Exception {super("cream");}
    }

    public static class CreamJobRunSandboxTest extends JobRunSandboxTest {
        public CreamJobRunSandboxTest() throws Exception {super("cream");}
    }
    
    public static class CreamJobRunInteractiveTest extends JobRunInteractiveTest {
    	public CreamJobRunInteractiveTest() throws Exception {super("cream");}
        public void test_setStdin_error() throws Exception { super.ignore("Cream status is DONE-OK"); }
    	public void test_run_environnement() throws Exception { super.ignore("Space is not supported in environment");   }
    }
}
