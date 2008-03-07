package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.job.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramExecutionFirstTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WSGramExecutionTestSuiteMinimal extends TestSuite {

    // test cases
    public static class WSGramJobRunMinimalTest extends JobRunMinimalTest {
        public WSGramJobRunMinimalTest() throws Exception {super("wsgram");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    
    public WSGramExecutionTestSuiteMinimal() throws Exception {
        super();
        // test cases
        this.addTestSuite(WSGramJobRunMinimalTest.class);
    }

    public static Test suite() throws Exception {
        return new WSGramExecutionTestSuiteMinimal();
    }
}