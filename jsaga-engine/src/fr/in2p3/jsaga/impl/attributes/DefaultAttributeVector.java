package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DefaultAttributeVector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 * This class is a friend of AbstractAttributesImpl
 */
public class DefaultAttributeVector extends DefaultAttributeAbstract implements AttributeVector {
    private String[] m_values;

    /** constructor for friend classes */
    DefaultAttributeVector(String key, boolean isSupported, boolean isReadOnly, String[] values) {
        super(key, isSupported, isReadOnly);
        m_values = values;
    }
    /** constructor */
    public DefaultAttributeVector(String key, String[] values) {
        super(key);
        m_values = values;
    }

    /** clone */
    public DefaultAttributeVector clone() throws CloneNotSupportedException {
        DefaultAttributeVector clone = (DefaultAttributeVector) super.clone();
        clone.m_values = new String[m_values.length];
        System.arraycopy(m_values, 0, clone.m_values, 0, m_values.length);
        return clone;
    }

    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        this.checkSupported();
        this.checkWritable();
        m_values = values;
    }
    
    public String[] getValues() throws NotImplementedException, IncorrectStateException {
        this.checkSupported();
        return m_values;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DefaultAttributeVector)) {
            return false;
        }
        DefaultAttributeVector attribute = (DefaultAttributeVector) o;
        if (!super.getKey().equals(attribute.getKey())) {
            return false;
        }
        if (m_values==null) {
            return (attribute.m_values==null);
        }
        if (attribute.m_values==null) {
            return (m_values==null);
        }
        if (m_values.length != attribute.m_values.length) {
            return false;
        }
        for (int i=0; i<m_values.length; i++) {
            String v1 = m_values[i];
            String v2 = attribute.m_values[i];
            if (v1==null) {
                if (v2 != null) {
                    return false;
                }
            } else {
                if (!v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }
}
