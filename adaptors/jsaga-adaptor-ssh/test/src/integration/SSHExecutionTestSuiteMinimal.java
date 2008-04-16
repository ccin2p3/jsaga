package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHExecutionTestSuiteMinimal
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/

public class SSHExecutionTestSuiteMinimal extends TestSuite {

    // test cases
    public static class SSHJobRunMinimalTest extends JobRunMinimalTest {
        public SSHJobRunMinimalTest() throws Exception {super("ssh");}
     }
    
    public SSHExecutionTestSuiteMinimal() throws Exception {
        super();
        // test cases
        this.addTestSuite(SSHJobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new SSHExecutionTestSuiteMinimal();
    }
}