package org.ogf.saga;

import org.ogf.saga.error.*;

import java.net.URI;
import java.net.URISyntaxException;

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
            this.fixFileURI();  //sreynaud
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in url", e);
        }
    }
    
    private URL(URI u) {
        this.u = u;
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
            this.fixFileURI();  //sreynaud
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in url", e);
        }
    }

    private void fixFileURI() throws URISyntaxException {
        if ("file".equals(u.getScheme()) && u.getAuthority()!=null && !u.getAuthority().equals(".")) {
            u = new URI(u.getScheme(), u.getUserInfo(), null, -1,
                    "/"+u.getAuthority()+u.getPath(), u.getQuery(), u.getFragment());
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
            throw new BadParameter("syntax error in fragment", e);
        }
    }

    /**
     * Returns the host part of this URL.
     * @return the host.
     */
    public String getHost() throws NotImplemented {
        if (u.getHost() == null) {
            return this.getSchemeSpecificPart().getHost();   //sreynaud
        }
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
            throw new BadParameter("syntax error in host", e);
        }
    }

    /**
     * Returns the path part of this URL.
     * @return the path.
     */
    public String getPath() throws NotImplemented {
        if (u.getPath() == null) {
            return this.getSchemeSpecificPart().getPath();  //sreynaud
        } else if (".".equals(u.getAuthority())) {
            return "."+u.getPath();                         //sreynaud
        } else if (u.getPath().startsWith("/./")) {
            return u.getPath().substring(1);                //sreynaud
        }
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
            if (path.startsWith("./")) {
                //sreynaud: set relative path
                u = new URI(u.getScheme(), u.getAuthority(),
                        path.substring(2), u.getQuery(), u.getFragment());
            } else {
                //sreynaud: fix absolute path
                int i;for(i=0; i<path.length() && path.charAt(i)=='/'; i++);
                if(i>1)path="/"+path.substring(i);
                u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                        u.getPort(), path, u.getQuery(), u.getFragment());
            }
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in path", e);
        }
    }

    /**
     * Returns the port number of this URL.
     * @return the port number.
     */
    public int getPort() throws NotImplemented {
        if (u.getPort() == -1) {
            return this.getSchemeSpecificPart().getPort();  //sreynaud
        }
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
            throw new BadParameter("syntax error in port", e);     // ???
        }
    }

    /**
     * Returns the query part from this URL.
     * @return the query.
     */
    public String getQuery() throws NotImplemented {
        if (u.getQuery() == null) {
            return this.getSchemeSpecificPart().getQuery(); //sreynaud
        }
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
            throw new BadParameter("syntax error in query", e);
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
            throw new BadParameter("syntax error in scheme", e);
        }
    }

    /**
     * Returns the userinfo part from this URL.
     * @return the userinfo.
     */
    public String getUserInfo() throws NotImplemented {
        if (u.getUserInfo() == null) {
            return this.getSchemeSpecificPart().getUserInfo();  //sreynaud
        }
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
            throw new BadParameter("syntax error in query", e);
        }
    }

    /**
     * Returns a new URL with the scheme part replaced.
     * @param scheme the new scheme.
     * @return the new URL.
     * @exception BadParameter is thrown when there is a syntax error in the
     *     new URL.
     */
    public URL translate(String scheme) throws NotImplemented, BadParameter,
           NoSuccess {
        try {
            URI url = new URI(scheme, u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
            // Not quite correct: the SAGA specs say that NoSuccess should be
            // thrown when the scheme is not supported. How to check this
            // here ???
            return new URL(url);
        } catch(URISyntaxException e) {
            throw new BadParameter("syntax error in scheme", e);
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
    
    /**
     * See {@link java.net.URI#resolve(java.net.URI)}.
     * @param url the url to resolve with respect to this one.
     * @return the resolved url.
     */
    public URL resolve(URL url) throws NoSuccess {
        URI uri = u.resolve(url.u);
        if (uri == url.u) {
            return url;
        }
        
        return new URL(uri);
    }
    
    /**
     * See {@link java.net.URI#isAbsolute()}.
     * @return whether this URL is an absolute URL.
     */
    public boolean isAbsolute() {
        return u.isAbsolute();
    }
    
    /**
     * See {@link java.net.URI#normalize()}.
     * @return a normalized URL.
     */
    public URL normalize() {
        URI uri = u.normalize();
        if (uri == u) {
            return this;
        }
        return new URL(uri);
    }

    private URI getSchemeSpecificPart() throws NotImplemented {
        try {
            return new URI(u.getRawSchemeSpecificPart());
        } catch (URISyntaxException e) {
            throw new NotImplemented(e);
        }
    }
}
