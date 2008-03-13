package integration;

import org.ogf.saga.job.JobRunWithPrequisiteTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramExecutionTestSuiteWithPrerequisite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U6ExecutionTestSuiteWithPrerequisite extends TestSuite {
    // test cases
    public static class U6JobRunWithPrequisiteTest extends JobRunWithPrequisiteTest {
        public U6JobRunWithPrequisiteTest() throws Exception {super("u6");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public U6ExecutionTestSuiteWithPrerequisite() throws Exception {
        super();
        // test cases
        this.addTestSuite(U6JobRunWithPrequisiteTest.class);
    }

    public static Test suite() throws Exception {
        return new U6ExecutionTestSuiteWithPrerequisite();
    }
}