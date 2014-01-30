package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.run.MinimalTest;

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
@RunWith(Suite.class)
@SuiteClasses({
    GkExecutionTestSuite.GkJobRunMinimalTest.class
})

public class GkExecutionTestSuite {
    // test cases
    public static class GkJobRunMinimalTest extends MinimalTest {
        public GkJobRunMinimalTest() throws Exception {super("gk");}
     }
}
