package org.ogf.saga.url;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

/**
 * Factory for URLs.
 */
public abstract class URLFactory {

    private static URLFactory factory;

    private static synchronized void initializeFactory()
            throws NoSuccessException, NotImplementedException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createURLFactory();
        }
    }

    protected abstract URL doCreateURL(String url)
            throws BadParameterException, NoSuccessException,
            NotImplementedException;

    /**
     * Creates an URL object from the specified string.
     * @param url the URL as a string.
     */
    public static URL createURL(String url)
            throws BadParameterException, NoSuccessException,
            NotImplementedException {
        initializeFactory();
        return factory.doCreateURL(url);
    }
}
