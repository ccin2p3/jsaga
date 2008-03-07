package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

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
public class WMSExecutionTestSuiteMinimal extends TestSuite {

    // test cases
    public static class WMSJobRunMinimalTest extends JobRunMinimalTest {
        public WMSJobRunMinimalTest() throws Exception {super("wms");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    public WMSExecutionTestSuiteMinimal() throws Exception {
        super();
        // test cases
        this.addTestSuite(WMSJobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new WMSExecutionTestSuiteMinimal();
    }
}