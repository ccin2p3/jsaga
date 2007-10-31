package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Attribute
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class Attribute implements Cloneable {
    private String m_key;
    private boolean m_isSupported;
    private boolean m_isReadOnly;

    ///////////////////////////////////////// public methods //////////////////////////////////////////

    /** constructor */
    protected Attribute(String key, boolean isSupported, boolean isReadOnly) {
        m_key = key;
        m_isSupported = isSupported;
        m_isReadOnly = isReadOnly;
    }
    /** constructor */
    public Attribute(String key) {
        this(key, true, false);
    }

    /** clone */
    public Object clone() throws CloneNotSupportedException {
        Attribute clone = (Attribute) super.clone();
        clone.m_key = m_key;
        clone.m_isSupported = m_isSupported;
        clone.m_isReadOnly = m_isReadOnly;
        return clone;
    }

    public String getKey() {
        return m_key;
    }
    public boolean isSupported() {
        return m_isSupported;
    }
    public boolean isReadOnly() {
        return m_isReadOnly;
    }

    public abstract void setValue(String value) throws NotImplemented, PermissionDenied, IncorrectState;
    public abstract String getValue() throws NotImplemented, IncorrectState;

    public abstract void setValues(String[] values) throws NotImplemented, PermissionDenied, IncorrectState;
    public abstract String[] getValues() throws NotImplemented, IncorrectState;

    public abstract boolean equals(Object o);

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkSupported() throws NotImplemented {
        if (!m_isSupported) {
            // as specified in SAGA document
            throw new NotImplemented("Attribute "+m_key+" not available in this implementation");
        }
    }
    protected void checkWritable() throws PermissionDenied {
        if (m_isReadOnly) {
            throw new PermissionDenied("Attribute "+m_key+" not writable");
        }
    }

    protected static String[] toVector(String value) {
        return (""+value).split(",");
    }
    protected static String toScalar(String[] values) {
        return StringArray.arrayToString(values, ",");
    }
}
