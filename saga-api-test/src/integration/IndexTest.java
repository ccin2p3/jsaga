package integration;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   index
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 mars 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
@Deprecated
public abstract class IndexTest extends TestCase {
    private Class m_class;

    public IndexTest(Class suite) {
        super();
        m_class = suite;
    }

    public void test_index() throws Exception {
        Class[] array = m_class.getClasses();
        for (int i=0; i<array.length; i++) {
            System.out.println(array[i].getName());
        }
    }
}
