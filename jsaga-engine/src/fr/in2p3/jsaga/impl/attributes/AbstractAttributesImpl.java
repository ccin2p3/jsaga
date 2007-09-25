package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.impl.AbstractSagaBaseImpl;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAttributesImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAttributesImpl extends AbstractSagaBaseImpl implements Attributes {
    private Map<String,Attribute> m_attributes;
    private boolean m_isExtensible;

    /** constructor */
    public AbstractAttributesImpl(Session session, boolean isExtensible) {
        super(session);
        m_attributes = new HashMap<String, Attribute>();
        m_isExtensible = isExtensible;
    }
    /** constructor */
    public AbstractAttributesImpl(Session session) {
        this(session, false);
    }

    /** constructor for deepCopy */
    protected AbstractAttributesImpl(AbstractAttributesImpl source) {
        super(source);
        m_attributes = deepCopy(source.m_attributes);
        m_isExtensible = source.m_isExtensible;
    }

    public String setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            attribute.setValue(value);
        } else if (m_isExtensible) {
            m_attributes.put(key, new AttributeScalar(key, value));
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
        return value;
    }

    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValue();
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public String[] setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            attribute.setValues(values);
        } else if (m_isExtensible) {
            m_attributes.put(key, new AttributeVector(key, values));
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
        return values;
    }

    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValues();
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public void removeAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, ReadOnly, Timeout, NoSuccess {
        if (!m_isExtensible) {
            throw new NoSuccess("Object does not support removing attributes", this);
        }
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (!attribute.isReadOnly()) {
                m_attributes.remove(key);
            } else {
                throw new ReadOnly("Attribute "+key+" not removable", this);
            }
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public String[] listAttributes() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return m_attributes.keySet().toArray(new String[m_attributes.keySet().size()]);
    }

    public String[] findAttributes(String keyPattern, String valuePattern) throws NotImplemented, BadParameter, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        List<String> foundKeys = new ArrayList<String>();
        for (Attribute attribute : m_attributes.values()) {
            if (keyPattern == null || attribute.getKey().matches(keyPattern)) {
                if (attribute.isSupported()) {
                    // supported attribute
                    boolean matched = false;
                    try {
                        if (attribute instanceof AttributeScalar) {
                            String value = attribute.getValue();
                            if (!matched) {
                                matched = (valuePattern == null || value.matches(valuePattern));
                            }
                        } else if (attribute instanceof AttributeVector) {
                            String[] values = attribute.getValues();
                            for (int i = 0; !matched && i < values.length; i++) {
                                matched = (valuePattern == null || values[i].matches(valuePattern));
                            }
                        }
                        if (matched) {
                            foundKeys.add(attribute.getKey());
                        }
                    } catch (IncorrectState incorrectState) {
                        // should never occur
                    }
                } else {
                    // unsupported attribute
                    if (valuePattern == null) {
                        foundKeys.add(attribute.getKey());
                    } else {
                        // unsupported attribute can not match
                    }
                }
            }
        }
        return foundKeys.toArray(new String[foundKeys.size()]);
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.isReadOnly();
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isWritableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return !attribute.isReadOnly();
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isRemoveableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return m_isExtensible;
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute instanceof AttributeVector;
        } else {
            throw new DoesNotExist("Attribute "+key+" does not exist", this);
        }
    }

    public boolean equals(AbstractAttributesImpl attributes) {
        return m_attributes.equals(attributes.m_attributes);
    }

    //////////////////////////////////////////// protected methods ////////////////////////////////////////////

    protected void _addUnsupportedAttribute(String key) {
        m_attributes.put(key, new AttributeScalar(key, false, false, null));
    }
    protected void _addReadOnlyAttribute(String key, String constantValue) {
        m_attributes.put(key, new AttributeScalar(key, true, true, constantValue));
    }
    protected void _addReadOnlyAttribute(String key, String[] constantValues) {
        m_attributes.put(key, new AttributeVector(key, true, true, constantValues));
    }
    protected Map _getAttributesMap() throws NotImplemented, IncorrectState {
        Map<String,String> map = new HashMap<String,String>();
        for (Map.Entry<String, Attribute> entry : m_attributes.entrySet()) {
            String key = entry.getKey();
            Attribute attr = entry.getValue();
            map.put(key, attr.getValue());
        }
        return map;
    }
    protected boolean _containsAttributeKey(String key) {
        return m_attributes.containsKey(key);
    }
    /**
     * To be used with fixed keys only
     */
    protected String _getOptionalAttribute(String key) throws NotImplemented, IncorrectState {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }
}
