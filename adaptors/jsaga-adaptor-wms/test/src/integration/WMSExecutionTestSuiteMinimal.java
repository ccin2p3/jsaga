package integration;

import junit.framework.Test;

import org.ogf.saga.job.JobListTest;
import org.ogf.saga.job.JobRunMinimalTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WMSExecutionTestSuiteMinimal extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new WMSExecutionTestSuiteMinimal();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(WMSExecutionTestSuiteMinimal.class);}}

    /** test cases */
    public static class WMSJobListTest extends JobListTest {
        public WMSJobListTest() throws Exception {super("wms");}
    }
    public static class WMSJobRunMinimalTest extends JobRunMinimalTest {
        public WMSJobRunMinimalTest() throws Exception {super("wms");}
    }

}