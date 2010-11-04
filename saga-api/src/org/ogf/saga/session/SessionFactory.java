package org.ogf.saga.session;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.NoSuccessException;

/**
 * Factory for creating sessions.
 */
public abstract class SessionFactory {

    private static SessionFactory factory;

    private synchronized static void initFactory() throws NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createSessionFactory();
        }
    }

    /**
     * Creates a session. To be provided by an implementation.
     * 
     * @param defaults
     *            when set, the default session is returned, with all the
     *            default contexts. Later modifications to this session are
     *            reflected in the default session.
     * @return the session.
     */
    protected abstract Session doCreateSession(boolean defaults)
            throws NoSuccessException;

    /**
     * Creates a session.
     * 
     * @param defaults
     *      when set, the default session is returned, with all the
     *      default contexts. Later modifications to this session are
     *      reflected in the default session.
     * @return
     *      the session.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Session createSession(boolean defaults)
            throws NoSuccessException {
        initFactory();
        return factory.doCreateSession(defaults);
    }

    /**
     * Returns the default session, with all the default contexts. Later
     * modifications to this session are reflected in the default session.
     * 
     * @return
     *      the session.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Session createSession() throws NoSuccessException {
        return createSession(true);
    }
}
