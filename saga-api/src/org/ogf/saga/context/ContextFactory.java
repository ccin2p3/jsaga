package org.ogf.saga.context;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/**
 * Factory for objects in the saga.context package.
 */
public abstract class ContextFactory {

    private static ContextFactory factory;

    /**
     * Constructs a security context. To be provided by the implementation.
     * 
     * @param type
     *            when set to a non-empty string is called.
     * @return the security context.
     */
    protected abstract Context doCreateContext(String type)
            throws IncorrectStateException, TimeoutException, NoSuccessException;

    private synchronized static void initFactory() throws NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createContextFactory();
        }
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
        initFactory();
        return factory.doCreateContext(type);
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
}
