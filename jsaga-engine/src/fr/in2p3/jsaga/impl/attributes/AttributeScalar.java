package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.IncorrectStateException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributeScalar
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 * This class is a friend of AbstractAttributesImpl
 */
public class AttributeScalar extends AttributeAbstract {
    private String m_value;

    /** constructor for friend classes */
    AttributeScalar(String key, boolean isSupported, boolean isReadOnly, String value) {
        super(key, isSupported, isReadOnly);
        m_value = value;
    }
    /** constructor */
    public AttributeScalar(String key, String value) {
        super(key);
        m_value = value;
    }
    /** constructor */
    public AttributeScalar(String key, String[] values) {
        super(key);
        m_value = toScalar(values);
    }

    /** clone */
    public Object clone() throws CloneNotSupportedException {
        AttributeScalar clone = (AttributeScalar) super.clone();
        clone.m_value = m_value;
        return clone;
    }

    /** override Attribute.setValues() */
    public void setValues(String[] values) throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+super.getKey()+" not vector");
    }

    /** override Attribute.getValues() */
    public String[] getValues() throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+super.getKey()+" not vector");
    }

    public void _setValue(String value) {
        m_value = value;
    }

    public String _getValue() {
        return m_value;
    }

    public void _setValues(String[] values) {
        m_value = toScalar(values);
    }

    public String[] _getValues() {
        return toVector(m_value);
    }

    public boolean equals(Object o) {
        if (!(o instanceof AttributeScalar)) {
            return false;
        }
        AttributeScalar attribute = (AttributeScalar) o;
        if (!super.getKey().equals(attribute.getKey())) {
            return false;
        }
        if (m_value==null) {
            return (attribute.m_value==null);
        }
        if (attribute.m_value==null) {
            return (m_value==null);
        }
        return m_value.equals(attribute.m_value);
    }
}
