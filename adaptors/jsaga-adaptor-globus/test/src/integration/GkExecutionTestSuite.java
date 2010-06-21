package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GkExecutionTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   21 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class GkExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new GkExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(GkExecutionTestSuite.class);}}

    // test cases
    public static class GkJobRunMinimalTest extends JobRunMinimalTest {
        public GkJobRunMinimalTest() throws Exception {super("gk");}
     }
}
