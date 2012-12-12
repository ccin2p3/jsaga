package fr.in2p3.jsaga.impl.context.attrs;

import fr.in2p3.jsaga.impl.attributes.Attribute;
import fr.in2p3.jsaga.impl.attributes.AttributeVector;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ServiceConfigAttribute
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public abstract class ServiceConfigAttribute implements AttributeVector {
    private Map<String, Properties> m_values;

    public ServiceConfigAttribute() {
        m_values = new HashMap<String, Properties>();
    }

    public boolean isReadOnly() {
        return false;
    }

    public MetricMode getMetricMode() {
        return MetricMode.Final;
    }

    public MetricType getMetricType() {
        return MetricType.String;
    }

    public Attribute clone() throws CloneNotSupportedException {
    	Class clazz;
		try {
			clazz = Class.forName(this.getClass().getName());
		} catch (ClassNotFoundException e1) {
			throw new CloneNotSupportedException();
		}

    	// Find the constructor
    	java.lang.reflect.Constructor constructor;
		try {
			constructor = clazz.getConstructor();
		} catch (SecurityException e) {
			throw new CloneNotSupportedException();
		} catch (NoSuchMethodException e) {
			throw new CloneNotSupportedException();
		}

    	// And construct
    	ServiceConfigAttribute clone;
		try {
			clone = (ServiceConfigAttribute)constructor.newInstance();
		} catch (IllegalArgumentException e) {
			throw new CloneNotSupportedException();
		} catch (InstantiationException e) {
			throw new CloneNotSupportedException();
		} catch (IllegalAccessException e) {
			throw new CloneNotSupportedException();
		} catch (InvocationTargetException e) {
			throw new CloneNotSupportedException();
		}
        clone.m_values = m_values;
        return clone;
    }

    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException, BadParameterException {
        m_values.clear();
    	for (String v : values) {
            int equals = v.indexOf('=');
            if (equals == -1) {
                throw new BadParameterException("Syntax error in expression: "+v);
            } else {
                // parse
                String service;
                String attrKey;
                String attrValue = v.substring(equals+1);
                String remains = v.substring(0, equals);
                int dot = remains.indexOf('.');
                if (dot == -1) {
                    service = "*";
                    attrKey = remains;
                } else {
                    service = remains.substring(0, dot);
                    attrKey = remains.substring(dot+1);
                }
                if ("".equals(service) || "".equals(attrKey)) {
                    throw new BadParameterException("Syntax error in expression: "+v);
                }

                // configure
                Properties config = m_values.get(service);
                if (config == null) {
                    config = new Properties();
                    m_values.put(service, config);
                }
                config.setProperty(attrKey, attrValue);
            }
        }
    }

    public String[] getValues() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        List<String> values = new ArrayList<String>();
        for (String serviceKey : m_values.keySet()) {
            Properties service = m_values.get(serviceKey);
            for (Object attrKey : service.keySet()) {
                String attrValue = service.getProperty((String) attrKey);
                String v = serviceKey+"."+attrKey+"="+attrValue;
                values.add(v);
            }
        }
        return values.toArray(new String[values.size()]);
    }

    public Properties getServiceConfig(String scheme) {
        if (m_values.containsKey("*")) {
            Properties clone = new Properties();
            clone.putAll(m_values.get("*"));
            if(m_values.get(scheme) != null){
            	clone.putAll(m_values.get(scheme)); // may override common attributes
            }
            return clone;
        } else {
            return m_values.get(scheme);
        }
    }
}
