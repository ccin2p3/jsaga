package fr.in2p3.jsaga.adaptor.evaluator;

import junit.framework.TestCase;
import org.ogf.saga.error.BadParameter;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BasicEvaluatorTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BasicEvaluatorTest extends TestCase {
    protected Evaluator m_evaluator;

    public BasicEvaluatorTest() {
        m_evaluator = new BasicEvaluator();
    }

    protected void setUp() throws Exception {
        m_evaluator.init(3);
    }

    protected void tearDown() throws Exception {
        m_evaluator = null;
    }

    public void test_basic() throws BadParameter {
        assertEquals(
                "3",
                m_evaluator.evaluate("INDICE"));
    }
}
