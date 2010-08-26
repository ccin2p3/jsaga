package fr.in2p3.jsaga.impl.attributes;

import fr.in2p3.jsaga.helpers.AttributeSerializer;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ScalarAttributeImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ScalarAttributeImpl<E> implements AttributeScalar {
    protected String m_key;
    protected E m_object;
    private MetricMode m_mode;
    private MetricType m_type;    

    /** constructor for SAGA implementation */
    public ScalarAttributeImpl(String key, String desc, MetricMode mode, MetricType type, E initialObject) {
        m_key = key;
        m_object = initialObject;
        m_mode = mode;
        m_type = type;
    }

    /** clone */
    public ScalarAttributeImpl<E> clone() throws CloneNotSupportedException {
        return new ScalarAttributeImpl<E>(m_key, null, m_mode, m_type, m_object);
    }

    ////////////////////////////////////// internal methods //////////////////////////////////////

    /** The SAGA engine implementation SHOULD use this method instead of method setValue. */
    public void setObject(E object) {
        m_object = object;
    }

    /** The SAGA engine implementation SHOULD use this method instead of method getValue. */
    public E getObject() {
        return m_object;
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

    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException {
        try {
            m_object = new AttributeSerializer<E>(m_type).fromString(value);
        } catch (DoesNotExistException e) {
            throw new NotImplementedException("INTERNAL ERROR: unexpected exception", e);
        }
    }

    public String getValue() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        try {
            return new AttributeSerializer<E>(m_type).toString(m_object);
        } catch (DoesNotExistException e) {
            throw new NotImplementedException("INTERNAL ERROR: unexpected exception", e);
        }
    }

    public void setValues(String[] values) throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+m_key+" not vector");
    }

    public String[] getValues() throws IncorrectStateException {
        throw new IncorrectStateException("Attribute "+m_key+" not vector");
    }

    public boolean equals(Object o) {
        if (o instanceof ScalarAttributeImpl) {
            ScalarAttributeImpl scalarAttribute = (ScalarAttributeImpl) o;
            return m_key.equals(scalarAttribute.getKey()) &&
                    (
                        (m_object ==null && scalarAttribute.getObject()==null) ||
                        (m_object !=null && m_object.equals(scalarAttribute.getObject()))
                    );
        } else {
            return false;
        }
    }
}
