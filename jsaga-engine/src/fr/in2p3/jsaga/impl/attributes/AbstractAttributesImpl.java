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
            attribute.setValue(value);
        } else if (m_isExtensible) {
            m_attributes.put(key, new AttributeScalar(key, value));
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValue();
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            attribute.setValues(values);
        } else if (m_isExtensible) {
            m_attributes.put(key, new AttributeVector(key, values));
        } else {
            throw new DoesNotExistException("Attribute "+key+" does not exist", this);
        }
    }

    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValues();
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
                                if (valueRegexp.matcher(attribute.getValue()).matches()) {
                                    return true;    //found
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

    public AttributeImpl _addAttribute(AttributeImpl attribute) {
        m_attributes.put(attribute.getKey(), attribute);
        return attribute;
    }

    protected void _addAttribute(String key, String value) {
        m_attributes.put(key, new AttributeScalar(key, value));
    }
    protected void _addVectorAttribute(String key, String values) {
        m_attributes.put(key, new AttributeVector(key, values));
    }
    protected void _addUnsupportedAttribute(String key) {
        m_attributes.put(key, new AttributeScalar(key, false, false, null));
    }
    public void _addReadOnlyAttribute(String key, String constantValue) {
        m_attributes.put(key, new AttributeScalar(key, true, true, constantValue));
    }
    public void _addReadOnlyAttribute(String key, String[] constantValues) {
        m_attributes.put(key, new AttributeVector(key, true, true, constantValues));
    }
    protected Map _getAttributesMap() throws NotImplementedException, IncorrectStateException {
        Map<String,String> map = new HashMap<String,String>();
        for (Map.Entry<String, Attribute> entry : m_attributes.entrySet()) {
            String key = entry.getKey();
            Attribute attr = entry.getValue();
            if (attr.getValue() != null) {
                map.put(key, attr.getValue());
            }
        }
        return map;
    }
    protected boolean _containsAttributeKey(String key) {
        return m_attributes.containsKey(key);
    }
    /**
     * To be used with fixed keys only
     */
    protected String _getOptionalAttribute(String key) throws NotImplementedException, IncorrectStateException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }

    /** For MetricImpl */
    protected boolean _changeAttribute(String key, String value) throws NoSuccessException {
        Attribute attribute = m_attributes.get(key);
        if (attribute != null) {
            try {
                if (attribute.getValue() != null) {
                    if (!attribute.getValue().equals(value)) {
                        attribute.setValue(value);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (value != null) {
                        attribute.setValue(value);
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch(SagaException e) {
                throw new NoSuccessException(e);
            }
        } else if (m_isExtensible) {
            m_attributes.put(key, new AttributeScalar(key, value));
            return false;   //initial value must not be notified
        } else {
            throw new NoSuccessException("Attribute "+key+" does not exist", this);
        }
    }
}
