package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DefaultAttributeScalar
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 * This class is a friend of AbstractAttributesImpl
 */
public class DefaultAttributeScalar extends DefaultAttributeAbstract implements AttributeScalar {
    private String m_value;

    /** constructor for friend classes */
    DefaultAttributeScalar(String key, boolean isSupported, boolean isReadOnly, String value) {
        super(key, isSupported, isReadOnly);
        m_value = value;
    }
    /** constructor */
    public DefaultAttributeScalar(String key, String value) {
        super(key);
        m_value = value;
    }

    /** clone */
    public DefaultAttributeScalar clone() throws CloneNotSupportedException {
        DefaultAttributeScalar clone = (DefaultAttributeScalar) super.clone();
        clone.m_value = m_value;
        return clone;
    }

    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        this.checkSupported();
        this.checkWritable();
        m_value = value;
    }

    public String getValue() throws NotImplementedException, IncorrectStateException {
        this.checkSupported();
        return m_value;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DefaultAttributeScalar)) {
            return false;
        }
        DefaultAttributeScalar attribute = (DefaultAttributeScalar) o;
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
