package org.glite.security.authz;

import javax.xml.rpc.handler.MessageContext;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Simple JAX-RPC MessageContext implementation to allow the authorization
 * framework to be used outside of a full JAX-RPC engine implementation.
 */
public class SimpleMessageContext implements MessageContext {
    private HashMap properties = new HashMap();
    /**
     * checks if context contains property.
     * @param name property name to look up
     * @return true if property is found, false otherwise
     */
    public boolean containsProperty(String name) {
        return this.properties.get(name) != null;
    }
    /**
     * gets property.
     * @param name property name to look up
     * @return property value or null if no property was found
     */
    public Object getProperty(String name) {
        return this.properties.get(name);
    }
    /**
     * gets the names of all properties in the context.
     * @return iterator over property names (Strings)
     */
    public Iterator getPropertyNames() {
        return this.properties.keySet().iterator();
    }
    /**
     * removes a property.
     * @param name the name of the property to be removed
     */
    public void removeProperty(String name) {
        this.properties.remove(name);
    }
    /**
     * sets a property.
     * @param name the name of the property to be set
     * @param value the value of the property
     */
    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }
}

