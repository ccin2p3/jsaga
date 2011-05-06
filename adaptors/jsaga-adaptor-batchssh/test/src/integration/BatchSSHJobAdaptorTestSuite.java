package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.NSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BatchSSHJobAdaptorTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BatchSSHJobAdaptorTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BatchSSHJobAdaptorTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BatchSSHJobAdaptorTestSuite.class);}}

}
