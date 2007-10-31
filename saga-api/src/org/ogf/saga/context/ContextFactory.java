package org.ogf.saga.context;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;



/**
 * Factory for objects in the saga.context package.
 */
public abstract class ContextFactory {
    
    private static ContextFactory factory;
    
    /**
     * Constructs a security context. To be provided by the implementation.
     * @param type when set to a non-empty string, {@link Context#setDefaults()}
     * is called.
     * @return the security context.
     */
    protected abstract Context doCreateContext(String type)
            throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    private synchronized static void initFactory() {
        if(factory == null) {
            factory = ImplementationBootstrapLoader.createContextFactory();    
        }
    }

    /**
     * Constructs a security context.
     * @param type when set to a non-empty string, {@link Context#setDefaults()}
     * is called.
     * @return the security context.
     */
    public static Context createContext(String type) throws NotImplemented,
           IncorrectState, Timeout, NoSuccess {
        initFactory();
        return factory.doCreateContext(type);
    }

    /**
     * Constructs a security context.
     * @return the security context.
     */
    public static Context createContext() throws NotImplemented,
           IncorrectState, Timeout, NoSuccess {
        return createContext("");
    }
}
