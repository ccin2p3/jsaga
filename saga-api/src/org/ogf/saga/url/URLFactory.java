package org.ogf.saga.url;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

/**
 * Factory for URLs.
 */
public abstract class URLFactory {

    private static URLFactory getFactory(String sagaFactoryName)
    		throws NoSuccessException {
	return ImplementationBootstrapLoader.getURLFactory(sagaFactoryName);
    }

    protected abstract URL doCreateURL(String url)
            throws BadParameterException, NoSuccessException;
       
    /**
     * Creates an URL object from the specified string.
     * @param
     *      url the URL as a string.
     * @exception BadParameterException
     *      is thrown when there is a syntax error in the parameter.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static URL createURL(String url)
            throws BadParameterException, NoSuccessException {
        return createURL(null, url);
    }
    
    /**
     * Creates an URL object from the specified string.
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param
     *      url the URL as a string.
     * @exception BadParameterException
     *      is thrown when there is a syntax error in the parameter.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static URL createURL(String sagaFactoryClassname, String url)
            throws BadParameterException, NoSuccessException {
        return getFactory(sagaFactoryClassname).doCreateURL(url);
    }
    
}
