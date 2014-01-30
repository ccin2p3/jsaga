package integration;

import junit.framework.TestSuite;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSAGATestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 mars 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
@Deprecated
public abstract class JSAGATestSuite extends TestSuite {
    public JSAGATestSuite() {
        super();

        // add test cases
        Class[] tests = this.getClass().getClasses();
        for (int i=0; i<tests.length; i++) {
            if (! IndexTest.class.equals(tests[i].getSuperclass())) {
                this.addTestSuite(tests[i]);
            }
        }
    }
}
