package org.glite.security.authz;

/**
 * This interface is used to encapsulate and shield the interceptor
 * configuration mechanism from the core PDP framework.
 * The configuration is associated with an interceptor between the
 * initialize and close calls.
 * @see ServiceInterceptor
 * @see ServicePIP
 * @see ServicePDP
 */
public interface ChainConfig {
    /**
     * gets the interceptors class names to be loaded, and their names
     * (configuration scopes).
     * @return array of interceptor configurations
     * @throws ConfigException if exception occured when retrieving
     *                         interceptors from configuration
     */
    InterceptorConfig[] getInterceptors() throws ConfigException;

    /**
     * gets a property based on the scoped name of the interceptor.
     * @param name scoped name of interceptor
     * @param property name of property to get
     * @return the property or null if not found
     */
    Object getProperty(String name, String property);

    /**
     * sets a property based on the scoped name of the interceptor.
     * @param name scoped name of interceptor
     * @param property name of property to set
     * @param value value of property to set
     */
    void setProperty(String name, String property, Object value);
}

