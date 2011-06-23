package org.ogf.saga.session;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.NoSuccessException;

/**
 * Factory for creating sessions.
 */
public abstract class SessionFactory {

    private static SessionFactory getFactory(String sagaFactoryName)
    		throws NoSuccessException {
	return ImplementationBootstrapLoader.getSessionFactory(sagaFactoryName);
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
	return createSession(null, defaults);
    }
    
    /**
     * Creates a session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
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
    public static Session createSession(String sagaFactoryClassname, boolean defaults)
            throws NoSuccessException {
	return getFactory(sagaFactoryClassname).doCreateSession(defaults);
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
    
    /**
     * Returns the default session, with all the default contexts. Later
     * modifications to this session are reflected in the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the session.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Session createSession(String sagaFactoryClassname) throws NoSuccessException {
        return createSession(sagaFactoryClassname, true);
    }
    
}
