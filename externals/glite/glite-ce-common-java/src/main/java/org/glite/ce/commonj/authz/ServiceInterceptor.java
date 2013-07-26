package org.glite.ce.commonj.authz;

/**
 * Generic interface to be implemented by all interceptors (PIPs and PDPs) in
 * a chain. a
 * chain.
 */
public interface ServiceInterceptor extends Cloneable {
    /**
     * initializes the interceptor with configuration information that are
     * valid up until the point when close is called.
     * @param config holding interceptor specific configuration values, that
     *               may be obtained using the name paramter
     * @param name the name that should be used to access all the interceptor
     *             local configuration
     * @param id the id in common for all interceptors in a chain (it is valid
     *          up until close is called)
     *          if close is not called the interceptor may assume that the id
     *          still exists after a process restart
     * @throws InitializeException if an exception occured during
     *                             initialization
     */
    void initialize(ChainConfig config, String name, String id) throws InitializeException;
    
    String getId();
    
    void setProperty(String name, String value) throws InitializeException;
    
    String getProperty(String name);
    
    String[] getProperties();
    
    boolean isTriggerable(String name);
    /**
     * this method is called by the PDP framework to indicate that the
     * interceptor now should remove all state that was allocated in the
     * initialize call.
     * @throws CloseException if an error occured while closing this
     *                       interceptor
     */
    void close() throws CloseException;
    
    public Object clone();
}

