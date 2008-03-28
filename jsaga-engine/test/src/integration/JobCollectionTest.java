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

    public void test_saga() throws Exception {super.launchTest("SAGA", "job.properties");}
    public void test_jsdl() throws Exception {super.launchTest("JSDL", "job.jsdl");}
    public void test_ref() throws Exception {super.launchTest();}
    public void test_sandbox() throws Exception {super.launchTest();}
    public void test_staging() throws Exception {super.launchTest();}
}
