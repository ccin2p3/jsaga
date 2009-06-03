package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributeAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AttributeAbstract implements Attribute {
    private String m_key;
    private boolean m_isSupported;
    private boolean m_isReadOnly;

    ///////////////////////////////////////// public methods //////////////////////////////////////////

    /** constructor */
    protected AttributeAbstract(String key, boolean isSupported, boolean isReadOnly) {
        m_key = key;
        m_isSupported = isSupported;
        m_isReadOnly = isReadOnly;
    }
    /** constructor */
    public AttributeAbstract(String key) {
        this(key, true, false);
    }

    /** clone */
    public AttributeAbstract clone() throws CloneNotSupportedException {
        AttributeAbstract clone = (AttributeAbstract) super.clone();
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

    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        this.checkSupported();
        this.checkWritable();
        this._setValue(value);
    }
    public String getValue() throws NotImplementedException, IncorrectStateException {
        this.checkSupported();
        return this._getValue();
    }
    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        this.checkSupported();
        this.checkWritable();
        this._setValues(values);
    }
    public String[] getValues() throws NotImplementedException, IncorrectStateException {
        this.checkSupported();
        return this._getValues();
    }

    /** The SAGA engine implementation SHOULD use this method instead of method setAttribute. */
    public abstract void _setValue(String value);

    /** The SAGA engine implementation SHOULD use this method instead of method getAttribute. */
    public abstract String _getValue();

    /** The SAGA engine implementation SHOULD use this method instead of method setVectorAttribute */
    public abstract void _setValues(String[] values);

    /** The SAGA engine implementation SHOULD use this method instead of method getVectorAttribute. */
    public abstract String[] _getValues();

    /** Attribute comparator */
    public abstract boolean equals(Object o);

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    private void checkSupported() throws NotImplementedException {
        if (!m_isSupported) {
            // as specified in SAGA document
            throw new NotImplementedException("Attribute "+m_key+" not available in this implementation");
        }
    }
    private void checkWritable() throws PermissionDeniedException {
        if (m_isReadOnly) {
            throw new PermissionDeniedException("Attribute "+m_key+" not writable");
        }
    }

    protected static String[] toVector(String value) {
        if (value != null) {
            String[] values = value.split(SEPARATOR);
            for (int i=0; i<values.length; i++) {
                values[i] = values[i].trim();
            }
            return values;
        } else {
            return null;
        }
    }
    protected static String toScalar(String[] values) {
        return StringArray.arrayToString(values, SEPARATOR);
    }
}
