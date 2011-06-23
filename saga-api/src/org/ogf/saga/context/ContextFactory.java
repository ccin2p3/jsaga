package org.ogf.saga.context;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/**
 * Factory for objects in the saga.context package.
 */
public abstract class ContextFactory {
    
    /**
     * Constructs a security context. To be provided by the implementation.
     * 
     * @param type
     *            when set to a non-empty string, {@link Context#setDefaults()}
     *            is called.
     * @return the security context.
     */
    protected abstract Context doCreateContext(String type)
            throws IncorrectStateException, TimeoutException, NoSuccessException;

    private static ContextFactory getFactory(String sagaFactoryName) throws NoSuccessException {
        return ImplementationBootstrapLoader.getContextFactory(sagaFactoryName);
    }

    /**
     * Constructs a security context.
     * 
     * @param type
     *      type of the context.
     * @return 
     *      the security context.
     * @exception IncorrectStateException
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Context createContext(String type)
            throws IncorrectStateException, TimeoutException, NoSuccessException {
	return createContext(null, type);
    }

    /**
     * Constructs a security context.
     * 
     * @return the security context.
     * @exception IncorrectStateException
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Context createContext() throws IncorrectStateException,
            TimeoutException, NoSuccessException {
        return createContext("");
    }
    
    /**
     * Constructs a security context.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param type
     *      type of the context.
     * @return 
     *      the security context.
     * @exception IncorrectStateException
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Context createContext(String sagaFactoryClassname, String type)
            throws IncorrectStateException, TimeoutException, NoSuccessException {
        return getFactory(sagaFactoryClassname).doCreateContext(type);
    }
}
