package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.helpers.AttributeSerializer;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VectorAttributeImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VectorAttributeImpl<E> implements AttributeVector {
    protected String m_key;
    protected E[] m_objects;
    private MetricMode m_mode;
    private MetricType m_type;

    /** constructor for SAGA implementation */
    public VectorAttributeImpl(String key, String desc, MetricMode mode, MetricType type, E[] initialObjects) {
        m_key = key;
        m_objects = initialObjects;
        m_mode = mode;
        m_type = type;
    }

    /** clone */
    public VectorAttributeImpl<E> clone() throws CloneNotSupportedException {
        VectorAttributeImpl<E> clone = (VectorAttributeImpl<E>) super.clone();
        clone.m_key = m_key;
        clone.m_objects = m_objects;
        clone.m_mode = m_mode;
        clone.m_type = m_type;
        return clone;
    }

    ////////////////////////////////////// internal methods //////////////////////////////////////

    /** The SAGA engine implementation SHOULD use this method instead of method setValues. */
    public void setObjects(E[] objects) {
        m_objects = objects;
    }

    /** The SAGA engine implementation SHOULD use this method instead of method getValues. */
    public E[] getObjects() {
        return m_objects;
    }

    ////////////////////////////////////// interface Attribute //////////////////////////////////////

    public String getKey() {
        return m_key;
    }

    public boolean isReadOnly() {
        switch(m_mode) {
            case ReadOnly:
            case Final:
                return true;
            case ReadWrite:
            default:
                return false;
        }
    }

    public void setValue(String value) throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+m_key+" not scalar");
    }

    public String getValue() throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+m_key+" not scalar");
    }

    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        AttributeSerializer<E> serializer = new AttributeSerializer<E>(m_type);
        m_objects = (E[]) new Object[values!=null ? values.length : 0];
        for (int i=0; i<m_objects.length; i++) {
            try {
                m_objects[i] = serializer.fromString(values[i]);
            } catch (DoesNotExistException e) {
                throw new NotImplementedException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public String[] getValues() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        AttributeSerializer<E> serializer = new AttributeSerializer<E>(m_type);
        String[] values = new String[m_objects!=null ? m_objects.length : 0];
        for (int i=0; i<values.length; i++) {
            try {
                values[i] = serializer.toString(m_objects[i]);
            } catch (DoesNotExistException e) {
                throw new NotImplementedException("INTERNAL ERROR: unexpected exception", e);
            }
        }
        return values;
    }

    public boolean equals(Object o) {
        if (o instanceof VectorAttributeImpl) {
            VectorAttributeImpl attribute = (VectorAttributeImpl) o;
            if (m_key.equals(attribute.getKey())) {
                if (m_objects != null) {
                    Object[] objects = attribute.getObjects();
                    if (objects!=null && m_objects.length==objects.length) {
                        for (int i=0; i<m_objects.length; i++) {
                            if (!m_objects[i].equals(objects[i])) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return attribute.getObjects() == null;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
