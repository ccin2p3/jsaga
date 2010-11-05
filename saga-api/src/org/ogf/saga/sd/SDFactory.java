package org.ogf.saga.sd;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;

/**
 * Factory for the creation of {@link Discoverer} objects. It is expected that
 * an implementation will have a method for obtaining a default value for the
 * URL of the information system.
 */
public abstract class SDFactory {

    private static SDFactory factory;

    private static synchronized void initializeFactory() throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createSDFactory();
        }
    }

    /**
     * Creates a <code>Discoverer</code> with the default <code>URL</code>.
     * To be provided by the implementation.
     * 
     * @param session
     *            the session handle
     * @return the discoverer
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws IncorrectURLException
     *             if an implementation cannot handle the specified protocol, or
     *             that access to the specified entity via the given protocol is
     *             impossible
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * 
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    protected abstract Discoverer doCreateDiscoverer(Session session) throws AuthenticationFailedException,
            AuthorizationFailedException, DoesNotExistException, IncorrectURLException, NoSuccessException,
            NotImplementedException, TimeoutException;

    /**
     * Creates a <code>Discoverer</code>. To be provided by the
     * implementation.
     * 
     * @param session
     *            the session handle
     * @param url
     *            the URL to guide the implementation
     * @return the discoverer
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws IncorrectURLException
     *             if an implementation cannot handle the specified protocol, or
     *             that access to the specified entity via the given protocol is
     *             impossible
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     * 
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    protected abstract Discoverer doCreateDiscoverer(Session session, URL url) throws AuthenticationFailedException,
            AuthorizationFailedException, DoesNotExistException, IncorrectURLException, NoSuccessException,
            NotImplementedException, TimeoutException;

    /**
     * Creates a <code>Discoverer</code> with the default <code>Session</code>
     * and <code>URL</code>.
     * 
     * @return the discoverer instance
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws IncorrectURLException
     *             if an implementation cannot handle the specified protocol, or
     *             that access to the specified entity via the given protocol is
     *             impossible
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    public static Discoverer createDiscoverer() throws AuthenticationFailedException, AuthorizationFailedException,
            DoesNotExistException, IncorrectURLException, NoSuccessException, NotImplementedException, TimeoutException {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateDiscoverer(session);
    }

    /**
     * Creates a <code>Discoverer</code>. The url specified as an input
     * parameter is to assist the implementation to locate the underlying
     * information system such that it can be queried.
     * 
     * @param session
     *            the session handle, may be <code>null</code>
     * @param url
     *            the URL to guide the implementation, may be <code>null</code>
     * @return the discoverer instance
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws IncorrectURLException
     *             if an implementation cannot handle the specified protocol, or
     *             that access to the specified entity via the given protocol is
     *             impossible
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    public static Discoverer createDiscoverer(Session session, URL url) throws AuthenticationFailedException,
            AuthorizationFailedException, DoesNotExistException, IncorrectURLException, NotImplementedException,
            NoSuccessException, TimeoutException {
        if (session == null) {
            session = SessionFactory.createSession();
        }
        initializeFactory();
        if (url == null) {
            return factory.doCreateDiscoverer(session);

        } else {
            return factory.doCreateDiscoverer(session, url);
        }
    }
}