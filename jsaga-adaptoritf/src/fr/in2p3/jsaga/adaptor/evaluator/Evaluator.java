package fr.in2p3.jsaga.adaptor.evaluator;

import org.ogf.saga.error.BadParameter;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Evaluator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Evaluator {
    public void init(int index);
    public String evaluate(String expression) throws BadParameter;
}
