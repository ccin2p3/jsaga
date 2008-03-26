package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataIntegrationTestSuite extends TestSuite {
    public DataIntegrationTestSuite() throws Exception {
        super();
        this.addTest(FileIntegrationTestSuite.suite());
        this.addTest(HttpIntegrationTestSuite.suite());
    }

    public static Test suite() throws Exception {
        return new DataIntegrationTestSuite();
    }
}
