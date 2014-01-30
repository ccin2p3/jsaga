package integration;

import org.junit.BeforeClass;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusExecutionTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusExecutionTestSuiteForWindows extends GlobusExecutionTestSuite {
    @BeforeClass
    public static void setType() {
        TYPE = "gatekeeper-windows";
    }
}