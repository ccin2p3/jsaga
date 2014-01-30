package integration;

import org.junit.BeforeClass;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LcgCeExecutionTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   17 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LcgCeExecutionTestSuite extends GlobusExecutionTestSuite {
    @BeforeClass
    public static void setType() {
        TYPE = "lcgce";
    }
}