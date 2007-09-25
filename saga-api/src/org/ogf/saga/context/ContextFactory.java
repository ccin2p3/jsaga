package org.ogf.saga.context;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;

/**
 * Factory for objects in the saga.context package.
 */
public abstract class ContextFactory {
    
    private static ContextFactory factory;
    
    /**
     * Constructs a security context. To be provided by the implementation.
     */
    protected abstract Context doCreateContext();

    private synchronized static void initFactory() {
        if(factory == null) {
            factory = ImplementationBootstrapLoader.createContextFactory();    
        }
    }

    /**
     * Constructs a security context.
     */
    public static Context createContext() {
        initFactory();
        return factory.doCreateContext();
    }
}
