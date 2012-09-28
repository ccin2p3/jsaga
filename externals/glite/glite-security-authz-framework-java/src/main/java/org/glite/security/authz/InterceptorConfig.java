package org.glite.security.authz;

/**
 * The <code>InterceptorConfig</code> class is used to hold configuration
 * information about an interceptor in a configuration mechanism independent
 * way. It is used by <code>ServicePDPConfig</code>.
 * @see ChainConfig
 */
public class InterceptorConfig {
    private String interceptorClass;
    private ServiceInterceptor interceptor;
    private String name;
    private boolean loaded = false;
    /**
     * Constructor.
     * @param configName the named scope of the interceptor used in
     *                   configuration entries
     * @param serviceInterceptor the class name of the interceptor
     */
    public InterceptorConfig(String configName,
                             ServiceInterceptor serviceInterceptor) {
        this.interceptor = serviceInterceptor;
        this.loaded = true;
        this.name = configName;
    }
    /**
     * Constructor.
     * @param configName the named scope of the interceptor used in
     *                   configuration entries
     * @param configInterceptorClass the class name of the interceptor
     */
    public InterceptorConfig(String configName,
                             String configInterceptorClass) {
        this.interceptorClass = configInterceptorClass;
        this.name = configName;
    }
    /**
     * gets the interceptor class.
     * @return the class name of the interceptor
     */
    public String getInterceptorClass() {
        return this.interceptorClass;
    }
    /**
     * gets the interceptor.
     * @return the interceptor
     */
    public ServiceInterceptor getInterceptor() {
        return this.interceptor;
    }
    /**
     * gets the interceptor name attached to this interceptor.
     * @return the named scope of the interceptor used in configuration
     *         entries
     */
    public String getName() {
        return this.name;
    }
    /**
     * method to support eager loading of interceptors.
     * @return whether the interceptor class has been loaded
     */
    public boolean isLoaded() {
        return this.loaded;
    }
}

