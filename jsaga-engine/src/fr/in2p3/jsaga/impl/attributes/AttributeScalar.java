package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

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
public class AttributeScalar extends Attribute {
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

    /** constructor for deepCopy */
    public AttributeScalar(AttributeScalar source) {
        super(source);
        m_value = source.m_value;
    }

    public void setValue(String value) throws NotImplemented, ReadOnly {
        checkSupported();
        checkWritable();
        m_value = value;
    }

    public String getValue() throws NotImplemented {
        checkSupported();
        return m_value;
    }

    public void setValues(String[] values) throws IncorrectState {
        throw new IncorrectState("Attribute "+super.getKey()+" not vector");
    }

    public String[] getValues() throws IncorrectState {
        throw new IncorrectState("Attribute "+super.getKey()+" not scalar");
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
