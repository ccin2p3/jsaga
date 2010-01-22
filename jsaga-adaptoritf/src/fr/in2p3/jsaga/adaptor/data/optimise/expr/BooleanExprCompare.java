package fr.in2p3.jsaga.adaptor.data.optimise.expr;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BooleanExprCompare
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BooleanExprCompare implements BooleanExpr {
    public static final int OPERATOR_EQUAL = 1;
    public static final int OPERATOR_NOT_EQUAL = 2;
    public static final int OPERATOR_GREATER_THAN = 3;
    public static final int OPERATOR_GREATER_OR_EQUAL = 4;
    public static final int OPERATOR_LOWER_THAN = 5;
    public static final int OPERATOR_LOWER_OR_EQUAL = 6;

    private int m_operator;
    private String m_key;
    private String m_value;

    public BooleanExprCompare(int operator, String key, String value) {
        m_operator = operator;
        m_key = key;
        m_value = value;
    }

    public int getOperator() {
        return m_operator;
    }

    public String getKey() {
        return m_key;
    }

    public String getValue() {
        return m_value;
    }

    public boolean isEqual() {return m_operator==OPERATOR_EQUAL;}
    public boolean isNotEqual() {return m_operator==OPERATOR_NOT_EQUAL;}
    public boolean isGreaterThan() {return m_operator==OPERATOR_GREATER_THAN;}
    public boolean isGreaterOrEqual() {return m_operator==OPERATOR_GREATER_OR_EQUAL;}
    public boolean isLowerThan() {return m_operator==OPERATOR_LOWER_THAN;}
    public boolean isLowerOrEqual() {return m_operator==OPERATOR_LOWER_OR_EQUAL;}
}
