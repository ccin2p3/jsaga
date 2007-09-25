package org.ogf.saga.session;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;

/**
 * Factory for creating sessions.
 */
public abstract class SessionFactory {
    
    private static SessionFactory factory;
    
    private synchronized static void initFactory(){
        if(factory == null) {
            factory = ImplementationBootstrapLoader.createSessionFactory();    
        } 
    }

    /**
     * Creates a session. To be provided by an implementation.
     * @param defaults when set, the default session is
     * returned, with all the default contexts. Later modifications to this
     * session are reflected in the default session.
     * @return the session.
     */
    protected abstract Session doCreateSession(boolean defaults);
    
    /**
     * Creates a session.
     * @param defaults when set, the default session is
     * returned, with all the default contexts. Later modifications to this
     * session are reflected in the default session.
     * @return the session.
     */
    public static Session createSession(boolean defaults) {
        initFactory();
        return factory.doCreateSession(defaults);
    }
}
