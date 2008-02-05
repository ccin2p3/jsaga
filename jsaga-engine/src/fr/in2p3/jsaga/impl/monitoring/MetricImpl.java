package fr.in2p3.jsaga.impl.monitoring;

import fr.in2p3.jsaga.helpers.AttributeSerializer;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import fr.in2p3.jsaga.impl.task.AbstractTaskImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogf.saga.ObjectType;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.*;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MetricImpl<E> extends AbstractAttributesImpl implements Metric {
    private static Log s_logger = LogFactory.getLog(MetricImpl.class);
    // attributes
    protected E m_value;
    // internal
    private AbstractTaskImpl m_task;
    private MetricMode m_mode;
    private MetricType m_type;
    private Map<Integer,Callback> m_callbacks;
    private int m_cookieGenerator;
    private boolean m_isListening;

    /** constructor */
    public MetricImpl(String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        super(null, true);   //not attached to a session, isExtensible=true
        // set attributes
        super._addReadOnlyAttribute(Metric.NAME, name);
        super._addReadOnlyAttribute(Metric.DESCRIPTION, desc);
        super._addReadOnlyAttribute(Metric.MODE, mode.name());
        super._addReadOnlyAttribute(Metric.UNIT, unit);
        super._addReadOnlyAttribute(Metric.TYPE, type.name());
        m_value = initialValue; //do not notify
        // internal
        m_mode = mode;
        m_type = type;
        m_callbacks = new HashMap<Integer,Callback>();
        m_cookieGenerator = 1;
        m_isListening = false;
    }

    /** clone */
    public MetricImpl<E> clone() throws CloneNotSupportedException {
        // clone attributes
        MetricImpl<E> clone = (MetricImpl<E>) super.clone();
        clone.m_value = m_value;
        // internal
        clone.m_task = m_task;
        clone.m_mode = m_mode;
        clone.m_type = m_type;
        clone.m_callbacks = clone(m_callbacks);
        clone.m_cookieGenerator = m_cookieGenerator;
        clone.m_isListening = m_isListening;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.METRIC;
    }

    public void setTask(AbstractTaskImpl task) {
        m_task = task;
    }

    public boolean isListening() {
        return m_isListening;
    }

    /**
     * If new value if different from old value, then set it and invoke callbacks.
     * The SAGA engine implementation SHOULD use this method instead of method setAttribute.
     * @param value the new value
     * @param mt the object that is setting this new value
     */
    public void setValue(E value, Monitorable mt) {
        boolean isModified = (m_value==null && value!=null) || (m_value!=null && !m_value.equals(value));
        if (isModified) {
            m_value = value;
            this.invokeCallbacks(mt);
        }
    }

    /**
     * The SAGA engine implementation SHOULD use this method instead of method getAttribute.
     * @return the current value
     */
    public E getValue() {
        return m_value;
    }

    /**
     * The SAGA engine implementation SHOULD use this method instead of method getAttribute.
     * @param defaultValue the default value
     * @return the current value if not null, else return the default value
     */
    public E getValue(E defaultValue) {
        return m_value!=null ? m_value : defaultValue;
    }

    /**
     * ReadWrite metrics MAY override this method if default behavior does not suit to your needs
     * @param value the value as a string
     * @return the value object
     * @throws NotImplemented if this method is not overrided
     */
    protected E getValuefromString(String value) throws NotImplemented, DoesNotExist {
        return new AttributeSerializer<E>(m_type).fromString(value);
    }

    /**
     * ReadWrite metrics MAY override this method if default behavior does not suit to your needs
     * @param values the value as a string array
     * @return the value object
     * @throws NotImplemented if this method is not overrided
     */
    protected E getValuefromStringArray(String[] values) throws NotImplemented, DoesNotExist {
        return new AttributeSerializer<E>(m_type).fromStringArray(values);
    }

    /**
     * Metrics MAY override this method if default behavior does not suit to your needs
     * @return the value as a string
     */
    protected String getStringFromValue() throws DoesNotExist {
        return new AttributeSerializer<E>(m_type).toString(m_value);
    }

    /**
     * Metrics MAY override this method if default behavior does not suit to your needs
     * @return the value as a string array
     */
    protected String[] getStringArrayFromValue() throws DoesNotExist {
        return new AttributeSerializer<E>(m_type).toStringArray(m_value);
    }

    //////////////////////////////////////////// interface Metric ////////////////////////////////////////////

    public int addCallback(Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        int cookie;
        switch (m_mode) {
            case ReadWrite:
            case ReadOnly:
                m_callbacks.put(m_cookieGenerator, cb);
                cookie = m_cookieGenerator++;
                break;
            case Final:
                throw new IncorrectState("Can not add callback to a metric with mode: "+m_mode.name(), this);
            default:
                throw new NoSuccess("INTERNAL ERROR: unexpected exception");
        }
        if (m_callbacks.size() > 0) {
            m_isListening = true;
            m_task.startListening(this);
        }
        return cookie;
    }

    public void removeCallback(int cookie) throws NotImplemented, BadParameter, AuthenticationFailed, AuthorizationFailed,PermissionDenied, Timeout, NoSuccess {
        if (m_callbacks.remove(cookie)!=null && m_callbacks.size()==0) {
            m_task.stopListening(this);
            m_isListening = false;
        }
    }

    public void fire() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        switch(m_mode) {
            case ReadWrite:
                throw new NotImplemented("Not implemented yet");    //todo: Implement method fire()
            case ReadOnly:
            case Final:
                throw new IncorrectState("Can not fire callback on a metric with mode: "+m_mode.name(), this);
            default:
                throw new NoSuccess("INTERNAL ERROR: unexpected exception");
        }
    }

    //////////////////////////////////////////// interface Attributes ////////////////////////////////////////////

    /** override Attributes.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (Metric.VALUE.equals(key)) {
            switch(m_mode) {
                case ReadWrite:
                    this.setValue(this.getValuefromString(value), null);
                    break;
                case ReadOnly:
                case Final:
                    throw new IncorrectState("Can not set attributes of a metric with mode: "+m_mode.name(), this);
                default:
                    throw new NoSuccess("INTERNAL ERROR: unexpected exception");
            }
        } else {
            super.setAttribute(key, value);
        }
    }

    /** override Attributes.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (Metric.VALUE.equals(key)) {
            return this.getStringFromValue();
        } else {
            return super.getAttribute(key);
        }
    }

    /** override Attributes.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (Metric.VALUE.equals(key)) {
            switch(m_mode) {
                case ReadWrite:
                    this.setValue(this.getValuefromStringArray(values), null);
                case ReadOnly:
                case Final:
                    throw new IncorrectState("Can not set attributes of a metric with mode: "+m_mode.name(), this);
                default:
                    throw new NoSuccess("INTERNAL ERROR: unexpected exception");
            }
        } else {
            super.setVectorAttribute(key, values);
        }
    }

    /** override Attributes.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (Metric.VALUE.equals(key)) {
            return this.getStringArrayFromValue();
        } else {
            return super.getVectorAttribute(key);
        }
    }

    //////////////////////////////////////////// private methods ////////////////////////////////////////////

    private void invokeCallbacks(Monitorable mt) {
        for (Iterator<Map.Entry<Integer,Callback>> it=m_callbacks.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer,Callback> entry = it.next();
            Integer cookie = entry.getKey();
            Callback callback = entry.getValue();
            try {
                boolean stayRegistered = callback.cb(mt, this, null);
                if (!stayRegistered) {
                    this.removeCallback(cookie);
                }
            } catch (Throwable e) {
                s_logger.warn("Failed to invoke callback: "+callback.getClass().getName(), e);
            }
        }
    }
}
