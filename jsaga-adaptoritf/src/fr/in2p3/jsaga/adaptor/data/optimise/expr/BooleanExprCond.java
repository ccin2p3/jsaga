package fr.in2p3.jsaga.adaptor.data.optimise.expr;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BooleanExprCond
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   22 janv. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class BooleanExprCond implements BooleanExpr {
    private List<BooleanExpr> m_list;

    public BooleanExprCond() {
        m_list = new ArrayList<BooleanExpr>();
    }

    public void add(BooleanExpr expr) {
        m_list.add(expr);
    }

    public int size() {
        return m_list.size();
    }

    public BooleanExpr get(int i) {
        return m_list.get(i);
    }

    public BooleanExpr[] get() {
        return m_list.toArray(new BooleanExpr[m_list.size()]);
    }
}
