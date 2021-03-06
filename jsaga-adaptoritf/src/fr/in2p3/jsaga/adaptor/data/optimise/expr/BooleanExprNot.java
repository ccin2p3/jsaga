package fr.in2p3.jsaga.adaptor.data.optimise.expr;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BooleanExprNot
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BooleanExprNot implements BooleanExpr {
    private BooleanExpr m_expr;

    public BooleanExprNot(BooleanExpr expr) {
        m_expr = expr;
    }

    public BooleanExpr get() {
        return m_expr;
    }
}
