package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
import org.ogf.saga.job.run.RequirementsTest;



/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    BesGenesisIITestSuite.BesGenesisIIJobRunRequiredTest.class,
    BesGenesisIITestSuite.BesGenesisIIJobRunOptionalTest.class,
    BesGenesisIITestSuite.BesGenesisIIJobRunDescriptionTest.class
})
public class BesGenesisIITestSuite {

    // test cases
    public static class BesGenesisIIJobDescriptionTest extends DescriptionTest {
        public BesGenesisIIJobDescriptionTest() throws Exception {super("bes-genesis2");}
     }
    
    // test cases
    public static class BesGenesisIIJobRunMinimalTest extends MinimalTest {
        public BesGenesisIIJobRunMinimalTest() throws Exception {super("bes-genesis2");}
    }
    
    // test cases
    public static class BesGenesisIIJobRunRequiredTest extends RequiredTest {
        public BesGenesisIIJobRunRequiredTest() throws Exception {super("bes-genesis2");}
        @Override @Test @Ignore("Not supported")
        public void test_run_error() { }
    }
    
    // test cases
    public static class BesGenesisIIJobRunOptionalTest extends OptionalTest {
        public BesGenesisIIJobRunOptionalTest() throws Exception {super("bes-genesis2");}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() {  }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {  }
        @Override @Test @Ignore("GenesisII does not provide job list in FactoryResourceAttributesDocument")
        public void test_listJob() {}
    }
    
 	// test cases
    // DataStaging not supported because of <jsaga> refused in JSDL
    //public static class BesGenesisIIJobRunSandboxTest extends JobRunSandboxTest {
    //    public BesGenesisIIJobRunSandboxTest() throws Exception {super("bes-genesis2");}
    //}
    
 	// test cases
    public static class BesGenesisIIJobRunDescriptionTest extends RequirementsTest {
        public BesGenesisIIJobRunDescriptionTest() throws Exception {super("bes-genesis2");}
        @Override @Test @Ignore("Not supported")
        public void test_run_queueRequirement() {  }
        @Override @Test @Ignore("Not supported")
        public void test_run_cpuTimeRequirement() {  }
    }

    // test cases
    // DataStaging not supported because of <jsaga> refused in JSDL
    //public static class BesGenesisIIJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public BesGenesisIIJobRunInteractiveTest() throws Exception {super("bes-genesis2");}
    //}
}