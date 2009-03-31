package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SuperSchedulerIntegrationTestSuite
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SuperSchedulerIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new SuperSchedulerIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(SuperSchedulerIntegrationTestSuite.class);}}

    /** test cases */
    public static class SuperSchedulerJobRunMinimalTest extends JobRunMinimalTest {
        public SuperSchedulerJobRunMinimalTest() throws Exception {super("naregi");}
    }
}
