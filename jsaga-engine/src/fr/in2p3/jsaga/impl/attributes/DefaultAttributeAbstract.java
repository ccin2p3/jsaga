package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DefaultAttributeAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DefaultAttributeAbstract implements Attribute {
    private String m_key;
    private boolean m_isSupported;
    private boolean m_isReadOnly;

    ///////////////////////////////////////// public methods //////////////////////////////////////////

    /** constructor */
    protected DefaultAttributeAbstract(String key, boolean isSupported, boolean isReadOnly) {
        m_key = key;
        m_isSupported = isSupported;
        m_isReadOnly = isReadOnly;
    }
    /** constructor */
    public DefaultAttributeAbstract(String key) {
        this(key, true, false);
    }

    /** clone */
    public DefaultAttributeAbstract clone() throws CloneNotSupportedException {
        DefaultAttributeAbstract clone = (DefaultAttributeAbstract) super.clone();
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

    /** Attribute comparator */
    public abstract boolean equals(Object o);

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkSupported() throws NotImplementedException {
        if (!m_isSupported) {
            // as specified in SAGA document
            throw new NotImplementedException("Attribute "+m_key+" not available in this implementation");
        }
    }
    protected void checkWritable() throws PermissionDeniedException {
        if (m_isReadOnly) {
            throw new PermissionDeniedException("Attribute "+m_key+" not writable");
        }
    }
}
