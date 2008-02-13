package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U implements Usage {
    protected int m_id;
    protected String m_name;

    public U(String name) {
        this(-1, name);
    }

    public U(int id, String name) {
        m_id = id;
        m_name = name;
    }

    /** Default implementation to override if needed */
    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist {
        if (m_name.equals(attributeName)) {
            try {
                this.throwExceptionIfInvalid(attributeValue);
                return attributeValue;
            } catch (Exception e) {
                return null;
            }
        } else {
            throw new DoesNotExist("Attribute not found: "+attributeName);
        }
    }

    public int getFirstMatchingUsage(Map attributes) throws DoesNotExist {
        if (attributes.containsKey(m_name)) {
            try {
                this.throwExceptionIfInvalid(attributes.get(m_name));
                return m_id;
            } catch (Exception e) {
                return -1;
            }
        } else {
            throw new DoesNotExist("Attribute not found: "+m_name);
        }
    }

    public final Usage getMissingValues(Map attributes) {
        try {
            this.throwExceptionIfInvalid(attributes.get(m_name));
            return null;
        } catch(Exception e) {
            return this;
        }
    }

    /**
     * To be overloaded if needed
     */
    public String toString() {
        return m_name;
    }

    /**
     * To be overloaded if needed
     */
    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        if (value == null) {
            throw new NullPointerException("Null value");
        }
        return value;
    }
}
