package fr.in2p3.jsaga.adaptor.evaluator;

import org.ogf.saga.error.BadParameter;

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
    private int m_indice;

    public void init(int indice) {
        m_indice = indice;
    }
    
    public String evaluate(String expression) throws BadParameter {
        if (expression.equals("INDICE")) {
            return ""+ m_indice;
        } else {
            throw new BadParameter("Expression is not supported: "+expression);
        }
    }
}
