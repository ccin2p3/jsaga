package integration;

import junit.framework.Test;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LcgCeExecutionTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   17 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LcgCeExecutionTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LcgCeExecutionTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LcgCeExecutionTestSuite.class);}}

    // test cases
    public static class LcgCeJobRunMinimalTest extends JobRunMinimalTest {
        public LcgCeJobRunMinimalTest() throws Exception {super("lcgce");}
     }
}