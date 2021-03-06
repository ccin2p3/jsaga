package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.helpers.cloner.AttributeCloner;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public abstract class AbstractAttributesImpl extends AbstractSagaObjectImpl implements Attributes {
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

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractAttributesImpl clone = (AbstractAttributesImpl) super.clone();
        clone.m_attributes = new AttributeCloner<String>().cloneMap(m_attributes);
        clone.m_isExtensible = m_isExtensible;
        return clone;
    }

    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (attribute instanceof AttributeScalar) {
                ((AttributeScalar)attribute).setValue(value);
            } else {
                throw new IncorrectStateException("Attempted to set scalar value on a vector attribute: "+key, this);
            }
        } else if (m_isExtensible) {
            m_attributes.put(key, new DefaultAttributeScalar(key, value));
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (attribute instanceof AttributeScalar) {
                return ((AttributeScalar)attribute).getValue();
            } else {
                throw new IncorrectStateException("Attempted to get scalar value from a vector attribute: "+key, this);
            }
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (attribute instanceof AttributeVector) {
                ((AttributeVector)attribute).setValues(values);
            } else {
                throw new IncorrectStateException("Attempted to set vector value on a scalar attribute: "+key, this);
            }
        } else if (m_isExtensible) {
            m_attributes.put(key, new DefaultAttributeVector(key, values));
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (attribute instanceof AttributeVector) {
                return ((AttributeVector)attribute).getValues();
            } else {
                throw new IncorrectStateException("Attempted to get vector value from a scalar attribute: "+key, this);
            }
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public void removeAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (!m_isExtensible) {
            throw new NoSuccessException("Object does not support removing attributes", this);
        }
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            if (!attribute.isReadOnly()) {
                m_attributes.remove(key);
            } else {
                throw new PermissionDeniedException("Attribute "+key+" not removable", this);
            }
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public String[] listAttributes() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_attributes.keySet().toArray(new String[m_attributes.keySet().size()]);
    }

    public String[] findAttributes(String... patterns) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        List<String> foundKeys = new ArrayList<String>();
        for (Attribute attribute : m_attributes.values()) {
            if (matches(attribute, patterns)) {
                foundKeys.add(attribute.getKey());
            }
        }
        return foundKeys.toArray(new String[foundKeys.size()]);
    }

    public boolean existsAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_attributes.containsKey(key);
    }

    private static final Pattern PATTERN = Pattern.compile("([^=]*)(=(.*))?");
    private static boolean matches(Attribute attribute, String... patterns) throws NotImplementedException, NoSuccessException {
        for (String pattern : patterns) {
            Matcher matcher = PATTERN.matcher(pattern);
            if (matcher.matches()) {
                String keyPattern = matcher.group(1);
                Pattern keyRegexp = SAGAPattern.toRegexp(keyPattern);
                if (keyRegexp == null) {
                    return true;    //found
                } else {
                    if (keyRegexp.matcher(attribute.getKey()).matches()) {
                        String valuePattern = matcher.group(3);
                        Pattern valueRegexp = SAGAPattern.toRegexp(valuePattern);
                        if (valueRegexp == null) {
                            return true;    //found
                        } else {
                            try {
                                if (attribute instanceof AttributeScalar) {
                                    String value = ((AttributeScalar)attribute).getValue();
                                    if (valueRegexp.matcher(value).matches()) {
                                        return true;    //found
                                    }
                                } else {
                                    String[] values = ((AttributeVector)attribute).getValues();
                                    for (String value : values) {
                                        if (valueRegexp.matcher(value).matches()) {
                                            return true;    //found (OR semantic)
                                        }
                                    }
                                }
                            } catch (IncorrectStateException e) {
                                throw new NoSuccessException(e);
                            }
                        }
                    }
                }
            }
        }
        return false;   //not found
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.isReadOnly();
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isWritableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return !attribute.isReadOnly();
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isRemovableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return m_isExtensible;
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public boolean isVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute instanceof AttributeVector;
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public boolean equals(AbstractAttributesImpl attributes) {
        return m_attributes.equals(attributes.m_attributes);
    }

    //////////////////////////////////////////// protected methods ////////////////////////////////////////////

    public AttributeScalar _addAttribute(AttributeScalar scalarAttribute) {
        m_attributes.put(scalarAttribute.getKey(), scalarAttribute);
        return scalarAttribute;
    }
    public AttributeVector _addVectorAttribute(AttributeVector vectorAttribute) {
        m_attributes.put(vectorAttribute.getKey(), vectorAttribute);
        return vectorAttribute;
    }
    public ScalarAttributeImpl _addAttribute(ScalarAttributeImpl scalarAttribute) {
        m_attributes.put(scalarAttribute.getKey(), scalarAttribute);
        return scalarAttribute;
    }
    public VectorAttributeImpl _addVectorAttribute(VectorAttributeImpl vectorAttribute) {
        m_attributes.put(vectorAttribute.getKey(), vectorAttribute);
        return vectorAttribute;
    }

    protected void _addAttribute(String key, String value) {
        m_attributes.put(key, new DefaultAttributeScalar(key, value));
    }
    protected void _addUnsupportedAttribute(String key) {
        m_attributes.put(key, new DefaultAttributeScalar(key, false, false, null));
    }
    public void _addReadOnlyAttribute(String key, String constantValue) {
        m_attributes.put(key, new DefaultAttributeScalar(key, true, true, constantValue));
    }
    public void _addReadOnlyAttribute(String key, String[] constantValues) {
        m_attributes.put(key, new DefaultAttributeVector(key, true, true, constantValues));
    }
    public Map _getAttributesMap() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        Map<String,String> map = new HashMap<String,String>();
        for (Map.Entry<String, Attribute> entry : m_attributes.entrySet()) {
            String key = entry.getKey();
            Attribute attr = entry.getValue();
            if (attr instanceof AttributeScalar) {
                String value = ((AttributeScalar)attr).getValue();
                if (value != null) {
                    map.put(key, value);
                }
            } else {
                // ignore vector attributes
            }
        }
        return map;
    }
    protected boolean _containsAttributeKey(String key) {
        return m_attributes.containsKey(key);
    }
}
