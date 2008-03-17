package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6ExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U6ExecutionTestSuiteMinimal extends TestSuite {

    // test cases
    public static class U6JobRunMinimalTest extends JobRunMinimalTest {
        public U6JobRunMinimalTest() throws Exception {super("unicore6");}
         public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    public U6ExecutionTestSuiteMinimal() throws Exception {
        super();
        // test cases
        this.addTestSuite(U6JobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new U6ExecutionTestSuiteMinimal();
    }
}