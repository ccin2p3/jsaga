package org.glite.security.authz;

import java.util.HashMap;

/**
 * Simple ChainConfig implementation allowing configuration
 * properties to be set at runtime.
 * @see ChainConfig
 */
public class SimpleChainConfig implements ChainConfig {
    private InterceptorConfig[] interceptors;
    private HashMap interceptorConfigs = new HashMap();

    /**
     * Constructor.
     * @param newInterceptors new interceptor configuration
     */
    public SimpleChainConfig(InterceptorConfig[] newInterceptors) {
        this.interceptors = newInterceptors;
        if (interceptors != null) {
            for (int i = 0; i < interceptors.length; i++) {
                interceptorConfigs.put(interceptors[i].getName(),
                                       new HashMap());
            }
        }
    }
    /**
     * gets the interceptors' class names to be loaded, and their names
     * (configuration scopes).
     * @return array of interceptor configurations
     * @throws ConfigException if no interceptors were found
     */
    public InterceptorConfig[] getInterceptors() throws ConfigException {
        if ((this.interceptors == null) || (this.interceptors.length == 0)) {
            throw new ConfigException("noInterceptors");
        }
        return this.interceptors;
    };
    /**
     * gets a property based on the scoped name of the interceptor.
     * @param name scoped name of interceptor
     * @param property name of property to get
     * @return the property or null if not found
     */
    public Object getProperty(String name, String property) {
        HashMap config = (HashMap) this.interceptorConfigs.get(name);
        if (config == null) {
            return null;
        }
        return config.get(property);
    }
   /**
     * sets a property based on the scoped name of the interceptor.
     * @param name scoped name of interceptor
     * @param property name of property to set
     * @param value value of property to set
     */
    public void setProperty(String name, String property, Object value) {
        HashMap config = (HashMap) this.interceptorConfigs.get(name);
        if (config == null) {
            return;
        }
        config.put(property, value);
    }
}

