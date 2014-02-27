package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.job.description.DescriptionTest;
import org.ogf.saga.job.run.InfoTest;
import org.ogf.saga.job.run.InteractiveTest;
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
    ArexTestSuite.ArexJobRunRequiredTest.class,
    ArexTestSuite.ArexJobRunOptionalTest.class,
    ArexTestSuite.ArexJobRunDescriptionTest.class,
    ArexTestSuite.ArexJobRunSandboxTest.class,
    ArexTestSuite.ArexJobRunInteractiveTest.class,
    ArexTestSuite.ArexJobRunInfoTest.class
})
public class ArexTestSuite {

    // test cases
    public static class ArexJobDescriptionTest extends DescriptionTest {
        public ArexJobDescriptionTest() throws Exception {super("arex");}
     }
    
    // test cases
    public static class ArexJobRunMinimalTest extends MinimalTest {
        public ArexJobRunMinimalTest() throws Exception {super("arex");}
    }
    
    // test cases
    public static class ArexJobRunRequiredTest extends RequiredTest {
        public ArexJobRunRequiredTest() throws Exception {super("arex");}
        @Override @Test @Ignore("Not supported")
        public void test_run_error() {}
    }
    
    // test cases
    public static class ArexJobRunOptionalTest extends OptionalTest {
        public ArexJobRunOptionalTest() throws Exception {super("arex");}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {}
        @Override @Test @Ignore("Not supported")
        public void test_resume_running() {}
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() { }
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {}
    }

    // test cases
    public static class ArexJobRunDescriptionTest extends RequirementsTest {
        public ArexJobRunDescriptionTest() throws Exception {super("arex");}
        @Override @Test @Ignore("Not supported")
        public void test_run_cpuTimeRequirement() throws Exception {}
    }

 	// test cases
    public static class ArexJobRunSandboxTest extends SandboxTest {
        public ArexJobRunSandboxTest() throws Exception {super("arex");}
    }
    
    // test cases
    public static class ArexJobRunInfoTest extends InfoTest {
    	public ArexJobRunInfoTest() throws Exception {super("arex");}
    }
    
    public static class ArexJobRunInteractiveTest extends InteractiveTest {
    	public ArexJobRunInteractiveTest() throws Exception {super("arex");}
    }

}