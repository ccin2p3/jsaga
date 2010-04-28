package fr.in2p3.jsaga.impl.monitoring;

import fr.in2p3.jsaga.helpers.AttributeSerializer;
import fr.in2p3.jsaga.helpers.cloner.ObjectCloner;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.apache.log4j.Logger;
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
    private static Logger s_logger = Logger.getLogger(MetricImpl.class);
    // attributes
    protected E m_value;
    // internal
    private Monitorable m_monitorable;
    private MetricMode m_mode;
    private MetricType m_type;
    private Map<Integer,Callback> m_callbacks;
    private int m_cookieGenerator;

    /** constructor */
    public MetricImpl(Monitorable mt, String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        super(null, true);   //not attached to a session, isExtensible=true
        // set attributes
        super._addReadOnlyAttribute(Metric.NAME, name);
        super._addReadOnlyAttribute(Metric.DESCRIPTION, desc);
        super._addReadOnlyAttribute(Metric.MODE, mode.name());
        super._addReadOnlyAttribute(Metric.UNIT, unit);
        super._addReadOnlyAttribute(Metric.TYPE, type.name());
        m_value = initialValue; //do not notify
        // internal
        m_monitorable = mt;
        m_mode = mode;
        m_type = type;
        m_callbacks = new HashMap<Integer,Callback>();
        m_cookieGenerator = 1;
    }

    /** clone */
    public MetricImpl<E> clone() throws CloneNotSupportedException {
        // clone attributes
        MetricImpl<E> clone = (MetricImpl<E>) super.clone();
        clone.m_value = m_value;
        // internal
        clone.m_monitorable = m_monitorable;
        clone.m_mode = m_mode;
        clone.m_type = m_type;
        clone.m_callbacks = new ObjectCloner<Integer,Callback>().cloneMap(m_callbacks);
        clone.m_cookieGenerator = m_cookieGenerator;
        return clone;
    }

    /**
     * If new value if different from old value, then set it and invoke callbacks.
     * The SAGA engine implementation SHOULD use this method instead of method setAttribute.
     * @param value the new value
     */
    public void setValue(E value) {
        boolean isModified = (m_value==null && value!=null) || (m_value!=null && !m_value.equals(value));
        if (isModified) {
            m_value = value;
            this.invokeCallbacks();
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
     * ReadWrite metrics MAY override this method if default behavior does not suit to your needs
     * @param value the value as a string
     * @return the value object
     * @throws NotImplementedException if this method is not overrided
     */
    protected E getValuefromString(String value) throws NotImplementedException, DoesNotExistException {
        return new AttributeSerializer<E>(m_type).fromString(value);
    }

    /**
     * ReadWrite metrics MAY override this method if default behavior does not suit to your needs
     * @param values the value as a string array
     * @return the value object
     * @throws NotImplementedException if this method is not overrided
     */
    protected E getValuefromStringArray(String[] values) throws NotImplementedException, DoesNotExistException {
        return new AttributeSerializer<E>(m_type).fromStringArray(values);
    }

    /**
     * Metrics MAY override this method if default behavior does not suit to your needs
     * @return the value as a string
     */
    protected String getStringFromValue() throws DoesNotExistException {
        return new AttributeSerializer<E>(m_type).toString(m_value);
    }

    /**
     * Metrics MAY override this method if default behavior does not suit to your needs
     * @return the value as a string array
     */
    protected String[] getStringArrayFromValue() throws DoesNotExistException {
        return new AttributeSerializer<E>(m_type).toStringArray(m_value);
    }

    //////////////////////////////////////////// interface Metric ////////////////////////////////////////////

    public int addCallback(Callback cb) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        switch (m_mode) {
            case ReadWrite:
            case ReadOnly:
                m_callbacks.put(m_cookieGenerator, cb);
                return m_cookieGenerator++;
            case Final:
                throw new IncorrectStateException("Can not add callback to a metric with mode: "+m_mode.name(), this);
            default:
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
        }
    }

    public void removeCallback(int cookie) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        m_callbacks.remove(cookie);
    }

    public void fire() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        switch(m_mode) {
            case ReadWrite:
                throw new NotImplementedException("Not implemented yet");    //todo: Implement method fire()
            case ReadOnly:
            case Final:
                throw new IncorrectStateException("Can not fire callback on a metric with mode: "+m_mode.name(), this);
            default:
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
        }
    }

    //////////////////////////////////////////// interface Attributes ////////////////////////////////////////////

    /** override Attributes.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Metric.VALUE.equals(key)) {
            switch(m_mode) {
                case ReadWrite:
                    this.setValue(this.getValuefromString(value));
                    break;
                case ReadOnly:
                case Final:
                    throw new IncorrectStateException("Can not set attributes of a metric with mode: "+m_mode.name(), this);
                default:
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
            }
        } else {
            super.setAttribute(key, value);
        }
    }

    /** override Attributes.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Metric.VALUE.equals(key)) {
            return this.getStringFromValue();
        } else {
            return super.getAttribute(key);
        }
    }

    /** override Attributes.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Metric.VALUE.equals(key)) {
            switch(m_mode) {
                case ReadWrite:
                    this.setValue(this.getValuefromStringArray(values));
                case ReadOnly:
                case Final:
                    throw new IncorrectStateException("Can not set attributes of a metric with mode: "+m_mode.name(), this);
                default:
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
            }
        } else {
            super.setVectorAttribute(key, values);
        }
    }

    /** override Attributes.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Metric.VALUE.equals(key)) {
            return this.getStringArrayFromValue();
        } else {
            return super.getVectorAttribute(key);
        }
    }

    //////////////////////////////////////////// private methods ////////////////////////////////////////////

    protected int getNumberOfCallbacks() {
        return m_callbacks.size();
    }

    protected void invokeCallbacks() {
        Collection<Map.Entry<Integer,Callback>> callbacks = new HashSet<Map.Entry<Integer,Callback>>(m_callbacks.entrySet());
        for (Map.Entry<Integer,Callback> entry : callbacks) {
            Integer cookie = entry.getKey();
            Callback callback = entry.getValue();
            try {
                boolean stayRegistered = callback.cb(m_monitorable, this, null);
                if (!stayRegistered) {
                    this.removeCallback(cookie);
                }
            } catch (Throwable e) {
                s_logger.warn("Failed to invoke callback: "+callback.getClass().getName(), e);
            }
        }
    }
}
