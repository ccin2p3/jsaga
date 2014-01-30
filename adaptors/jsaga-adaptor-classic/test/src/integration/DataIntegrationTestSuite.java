package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
@RunWith(Suite.class)
@SuiteClasses({
    FileIntegrationTestSuite.class,
    ZipIntegrationTestSuite.class,
    HttpIntegrationTestSuite.class,
    HttpsIntegrationTestSuite.class
})

public class DataIntegrationTestSuite {
}
