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
import org.ogf.saga.job.run.SandboxTest;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    BesUnicoreTestSuite.BesUnicoreJobRunRequiredTest.class,
    BesUnicoreTestSuite.BesUnicoreJobRunOptionalTest.class,
    BesUnicoreTestSuite.BesUnicoreJobRunDescriptionTest.class,
    BesUnicoreTestSuite.BesUnicoreJobRunSandboxTest.class
})
public class BesUnicoreTestSuite {

    // test cases
    public static class BesUnicoreJobDescriptionTest extends DescriptionTest {
        public BesUnicoreJobDescriptionTest() throws Exception {super("bes-unicore");}
        public void test_wallTimeLimit() { }
     }
    
    // test cases
    public static class BesUnicoreJobRunMinimalTest extends MinimalTest {
        public BesUnicoreJobRunMinimalTest() throws Exception {super("bes-unicore");}
    }
    
    // test cases
    public static class BesUnicoreJobRunRequiredTest extends RequiredTest {
        public BesUnicoreJobRunRequiredTest() throws Exception {super("bes-unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_run_error() { }
    }
    
    // test cases
    public static class BesUnicoreJobRunOptionalTest extends OptionalTest {
        public BesUnicoreJobRunOptionalTest() throws Exception {super("bes-unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() { }
        @Override @Test @Ignore("Not supported")
        public void test_resume_running() { }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() { }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() { }
    }
    
 	// test cases
    public static class BesUnicoreJobRunSandboxTest extends SandboxTest {
        public BesUnicoreJobRunSandboxTest() throws Exception {super("bes-unicore");}
    }
    
 	// test cases
    public static class BesUnicoreJobRunDescriptionTest extends RequirementsTest {
        public BesUnicoreJobRunDescriptionTest() throws Exception {super("bes-unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_run_inWorkingDirectory() { }
        @Override @Test @Ignore("Not supported")
        public void test_run_queueRequirement() { }
        @Override @Test @Ignore("Not supported")
        public void test_run_cpuTimeRequirement() { }
    }

    /* incompatible with Unicore Data plugin */
    // test cases
    //public static class BesUnicoreJobRunInteractiveTest extends JobRunInteractiveTest {
    //    public BesUnicoreJobRunInteractiveTest() throws Exception {super("bes-unicore");}
    //}
}