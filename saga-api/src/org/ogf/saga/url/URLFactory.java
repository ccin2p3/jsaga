package org.ogf.saga.url;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.url.URL;

/**
 * Factory for URLs.
 */
public abstract class URLFactory {

    private static URLFactory factory;

    private static synchronized void initializeFactory()
            throws NoSuccess, NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createURLFactory();
        }
    }

    protected abstract URL doCreateURL(String url)
            throws BadParameter, NoSuccess,
            NotImplemented;

    /**
     * Creates an URL object from the specified string.
     * @param url the URL as a string.
     */
    public static URL createURL(String url)
            throws BadParameter, NoSuccess,
            NotImplemented {
        initializeFactory();
        return factory.doCreateURL(url);
    }
}
