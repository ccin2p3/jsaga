package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ogf.saga.job.JobRunSandboxTest;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProtocolIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class OurGridTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new OurGridTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(OurGridTestSuite.class);}}

    /** test cases */
    public static class OurGridNSEntryTest extends NSEntryTest {
        public OurGridNSEntryTest() throws Exception {super("ourgrid");}
        public void test_unexisting() { super.ignore("not yet implemented"); }
    }
    /** test cases */
    public static class OurGridJobRunSandboxTest extends JobRunSandboxTest {
        public OurGridJobRunSandboxTest() throws Exception {super("ourgrid");}
        public void test_remote_input_explicit() throws Exception { super.ignore("Not supported yet."); }
        public void test_remote_output_explicit() throws Exception { super.ignore("Not supported yet."); }
        public void test_input_output_explicit() throws Exception { super.ignore("Not supported yet."); }
        public void test_input_output_implicit() throws Exception { super.ignore("Not supported yet."); }
//        public void test_output_only_implicit() throws Exception { super.ignore("TODO: explain why this test is ignored..."); }
    
}
}