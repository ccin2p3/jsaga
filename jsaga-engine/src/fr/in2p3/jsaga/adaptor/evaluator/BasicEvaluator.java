package fr.in2p3.jsaga.adaptor.evaluator;

import org.ogf.saga.error.BadParameterException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BasicEvaluator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BasicEvaluator implements Evaluator {
    private int m_index;

    public void init(int index) {
        m_index = index;
    }
    
    public String evaluate(String expression) throws BadParameterException {
        if (expression.equals("INDEX")) {
            return ""+ m_index;
        } else {
            throw new BadParameterException("Expression is not supported: "+expression);
        }
    }
}
