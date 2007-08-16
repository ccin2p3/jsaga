package fr.in2p3.jsaga.engine.base;

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
public abstract class Attribute extends AbstractSagaBaseImpl {
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

    /** constructor for deepCopy */
    protected Attribute(Attribute source) {
        super(source);
        m_key = source.m_key;
        m_isSupported = source.m_isSupported;
        m_isReadOnly = source.m_isReadOnly;
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

    public abstract void setValue(String value) throws NotImplemented, ReadOnly, IncorrectState;
    public abstract String getValue() throws NotImplemented, IncorrectState;

    public abstract void setValues(String[] values) throws NotImplemented, ReadOnly, IncorrectState;
    public abstract String[] getValues() throws NotImplemented, IncorrectState;

    public abstract boolean equals(Object o);

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkSupported() throws NotImplemented {
        if (!m_isSupported) {
            // as specified in SAGA document
            throw new NotImplemented("Attribute "+m_key+" not available in this implementation", this);
        }
    }
    protected void checkWritable() throws ReadOnly {
        if (m_isReadOnly) {
            throw new ReadOnly("Attribute "+m_key+" not writable", this);
        }
    }

    protected static String[] toVector(String value) {
        return (""+value).split(",");
    }
    protected static String toScalar(String[] values) {
        return StringArray.arrayToString(values, ",");
    }
}
