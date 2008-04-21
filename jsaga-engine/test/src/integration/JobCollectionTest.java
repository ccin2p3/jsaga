package integration;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionTest extends AbstractJobCollectionTest {
    public JobCollectionTest() throws Exception {
        super();
    }

    // check language translation
    public void test_language() throws Exception {
        super.checkLanguage("JSDL", "JobCollection.xml");
        super.checkLanguage("JSDL", "Job.xml");
        super.checkLanguage("JSDL", "JobDefinition.jsdl");
    }
    public void test_saga() throws Exception {
        super.checkLanguage("SAGA", "job.properties");
    }

    // check job submission
    public void test_submit() throws Exception {
        super.checkSubmit("job.xml");
//        super.checkSubmit("job-sandbox.xml");
//        super.checkSubmit("job-staging.xml");
    }

    // check job transformation
//    public void test_ref() throws Exception {super.checkPreprocess();}
    public void test_sandbox() throws Exception {super.checkPreprocess();}
    public void test_staging() throws Exception {super.checkPreprocess();}
}
