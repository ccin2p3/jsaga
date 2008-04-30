package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalExecutionTestSuiteMinimal
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   9=29 avril 2008
****************************************************/

public class LocalExecutionTestSuiteMinimal extends TestSuite {

    // test cases
    public static class LocalJobRunMinimalTest extends JobRunMinimalTest {
        public LocalJobRunMinimalTest() throws Exception {super("local");}
     }
    
    public LocalExecutionTestSuiteMinimal() throws Exception {
        super();
        // test cases
        this.addTestSuite(LocalJobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new LocalExecutionTestSuiteMinimal();
    }
}