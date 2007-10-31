package org.ogf.saga;

import java.net.URISyntaxException;
import java.net.URI;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

/**
 * URL class as specified by SAGA. The java.net.URL class is not usable
 * because of all kinds of side-effects.
 * TODO: provide factory with this as a default implementation???
 */
public class URL {
    private URI u;

    /**
     * Constructs an URL from the specified string.
     * @param url the string.
     * @exception BadParameter is thrown when there is a syntax error
     *     in the parameter.
     */
    public URL(String url) throws NotImplemented, BadParameter, NoSuccess {
        try {
            u = new URI(url);
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in url");
        }
    }

    /**
     * Replaces the current value of the URL with the specified value.
     * @param url the string.
     * @exception BadParameter is thrown when there is a syntax error
     *     in the parameter.
     */
    public void setURL(String url) throws NotImplemented, BadParameter {
        try {
            u = new URI(url);
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in url");
        }
    }

    /**
     * Returns this URL as a string.
     * @return the string.
     */
    public String getURL() throws NotImplemented {
        return toString();
    }
    
    /**
     * Returns the fragment part of this URL.
     * @return the fragment.
     */
    public String getFragment() throws NotImplemented {
        return u.getFragment();
    }

    /**
     * Sets the fragment part of this URL to the specified parameter.
     * @param fragment the fragment.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter.
     */
    public void setFragment(String fragment) throws NotImplemented,
           BadParameter {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), fragment);
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in fragment");
        }
    }

    /**
     * Returns the host part of this URL.
     * @return the host.
     */
    public String getHost() throws NotImplemented {
        return u.getHost();
    }

    /**
     * Sets the host part of this URL to the specified parameter.
     * @param host the host.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter.
     */
    public void setHost(String host) throws NotImplemented, BadParameter {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), host,
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in host");
        }
    }

    /**
     * Returns the path part of this URL.
     * @return the path.
     */
    public String getPath() throws NotImplemented {
        return u.getPath();
    }

    /**
     * Sets the path part of this URL to the specified parameter.
     * @param path the path.
     * @exception BadParameter is thrown when there is a syntax error in the
     * path.
     */
    public void setPath(String path) throws NotImplemented, BadParameter {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), path, u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in host");
        }
    }

    /**
     * Returns the port number of this URL.
     * @return the port number.
     */
    public int getPort() throws NotImplemented {
        return u.getPort();
    }

    /**
     * Sets the port number of this URL to the specified parameter.
     * @param port the port number.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter. (???)
     */
    public void setPort(int port) throws NotImplemented, BadParameter {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    port, u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in port");     // ???
        }
    }

    /**
     * Returns the query part from this URL.
     * @return the query.
     */
    public String getQuery() throws NotImplemented {
        return u.getQuery();
    }

    /**
     * Sets the query part of this URL to the specified parameter.
     * @param query the query.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter.
     */
    public void setQuery(String query) throws NotImplemented, BadParameter {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), query, u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in query");
        }
    }

    /**
     * Returns the scheme part from this URL.
     * @return the scheme.
     */
    public String getScheme() throws NotImplemented {
        return u.getScheme();
    }

    /**
     * Sets the scheme part of this URL to the specified parameter.
     * @param scheme the scheme.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter.
     */
    public void setScheme(String scheme) throws NotImplemented, BadParameter {
        try {
            u = new URI(scheme, u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in scheme");
        }
    }

    /**
     * Returns the userinfo part from this URL.
     * @return the userinfo.
     */
    public String getUserInfo() throws NotImplemented {
        return u.getUserInfo();
    }

    /**
     * Sets the user info part of this URL to the specified parameter.
     * @param userInfo the userinfo.
     * @exception BadParameter is thrown when there is a syntax error in the
     * parameter.
     */
    public void setUserInfo(String userInfo) throws NotImplemented,
           BadParameter {
        try {
            u = new URI(u.getScheme(), userInfo, u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in query");
        }
    }

    /**
     * Returns a new URL with the scheme part replaced.
     * @param scheme the new scheme.
     * @return the new URL.
     * @exception BadParameter is thrown when there is a syntax error in the
     *     new URL.
     */
    public String translate(String scheme) throws NotImplemented, BadParameter,
           NoSuccess {
        try {
            URI url = new URI(scheme, u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
            // Not quite correct: the SAGA specs say that NoSuccess should be
            // thrown when the scheme is not supported. How to check this
            // here ???
            return url.toString();
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in scheme");
        }
    }

    public int hashCode() {
        return u.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof URL)) {
            return false;
        }
        URL other = (URL) o;
        return u.equals(other.u);
    }

    public String toString() {
        return u.toString();
    }
}
