package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.url.URL;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbsoluteURLImpl
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 fév 2011
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AbsoluteURLImpl extends AbstractURLImpl implements URL {
    protected URI u;
    public final static String ABSOLUTE_URL_REGEXP = "^[^/\\\\]{2,}\\:/.*";
    
    /** MAY encode the URL */
    AbsoluteURLImpl(String url) throws BadParameterException {
    	this.setString(url);
    }

    AbsoluteURLImpl(FileAttributes cache) throws BadParameterException {
        this(cache.getRelativePath());
        m_cache = cache;
    }

    /** DO NOT encode the URL */
    protected AbsoluteURLImpl(URI u) throws BadParameterException {
        this.u = u;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbsoluteURLImpl clone = (AbsoluteURLImpl) super.clone();
        clone.u = u;
        clone.m_cache = m_cache;
        return clone;
    }

    /** Encode the URL */
    public void setString(String url) throws BadParameterException {
        if (url == null) {
            url = "";
        }
        // Check that url is absolute
        if (url != "" && ! Pattern.matches(ABSOLUTE_URL_REGEXP, url)) {
    		throw new BadParameterException("URL must be absolute");
    	}
        String encodedUrl = (URLHelper.startsWithLocalScheme(url))
                ? URLEncoder.encodePathOnly(url)
                : URLEncoder.encode(url);
        try {
            u = new URI(encodedUrl);
            // fix path
            setPath(getPath());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in url", e);
        }
    }

    /** Decode the URL */
    public String getString() {
        StringBuffer buffer = new StringBuffer();
        if (getScheme() != null) {
            buffer.append(getScheme());
            buffer.append(":");
        }
        if (getHost() != null) {
            if (getScheme() != null) buffer.append("//");
            if (getUserInfo() != null) {
                buffer.append(getUserInfo());
                buffer.append('@');
            }
            buffer.append(getHost());
            if (getPort() != -1) {
                buffer.append(':');
                buffer.append(getPort());
            }
        }
        if (getPath() != null) {
            buffer.append(getPath());
        }
        if (getQuery() != null) {
            buffer.append('?');
            buffer.append(getQuery());
        }
        if (getFragment() != null) {
            buffer.append('#');
            buffer.append(getFragment());
        }
        return buffer.toString();
    }
    
    /** DO NOT decode the URL */
    public String getEscaped() {
        return u.toString();
    }
    /** DO NOT decode the URL */
    public String toString() {
        return u.toString();
    }

    public String getFragment() {
        return u.getFragment();
    }

    public void setFragment(String fragment) throws BadParameterException {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), fragment);
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in fragment", e);
        }
    }

    public String getHost() {
        if (u.getHost() == null) {
        	// TODO: check this
            return this.getSchemeSpecificPart().getHost();   //sreynaud
        }
        return u.getHost();
    }

    public void setHost(String host) throws BadParameterException {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), host,
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in host", e);
        }
    }

    public String getPath() {
        if (u.getPath() == null) {
            return this.getSchemeSpecificPart().getPath();  //sreynaud
        }
        return u.getPath();
    }

    public void setPath(String path) throws BadParameterException {
        if (path == null) {
            path = "";
        }
		// convert '\' to '/'
		if (System.getProperty("file.separator") != "/")
			path = path.replace(System.getProperty("file.separator"), "/");
        try {
        	// remove duplicate leading /
            int i;for(i=0; i<path.length() && path.charAt(i)=='/'; i++);
            if(i>1)path="/"+path.substring(i);
        	// add leading / in case of Windoze path like X:/...
        	if (Pattern.matches("^[^/\\\\]{1}\\:.*", path))
        		path = "/"+path;
            if (path == "" && u.getRawAuthority() == null) 
            	throw new BadParameterException("Path cannot by empty if authority is empty");
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), path, u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in path", e);
        }
    }

    public int getPort() {
        if (u.getPort() == -1) {
            return this.getSchemeSpecificPart().getPort();  //sreynaud
        }
        return u.getPort();
    }

    public void setPort(int port) throws BadParameterException {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    port, u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in port", e);     // ???
        }
    }

    public String getQuery() {
        if (u.getQuery() == null) {
            return this.getSchemeSpecificPart().getQuery(); //sreynaud
        }
        return u.getQuery();
    }

    public void setQuery(String query) throws BadParameterException {
        try {
            u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), query, u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in query", e);
        }
    }

    public String getScheme() {
        return u.getScheme();
    }

    public void setScheme(String scheme) throws BadParameterException {
        try {
            u = new URI(scheme, u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in scheme", e);
        }
    }

    public String getUserInfo() {
        if (u.getUserInfo() == null) {
            return this.getSchemeSpecificPart().getUserInfo();  //sreynaud
        }
        return u.getUserInfo();
    }

    public void setUserInfo(String userInfo) throws BadParameterException {
        try {
            u = new URI(u.getScheme(), userInfo, u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in query", e);
        }
    }

    public URL translate(String scheme) throws BadParameterException, NoSuccessException {
        try {
            URI url = new URI(scheme, u.getUserInfo(), u.getHost(),
                    u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
            // Not quite correct: the SAGA specs say that NoSuccessException should be
            // thrown when the scheme is not supported. How to check this
            // here ???
            return new AbsoluteURLImpl(url);
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in scheme", e);
        }
    }

    public URL resolve(URL url) throws NoSuccessException {
    	URI uri;
    	if (url instanceof AbsoluteURLImpl) {
    		AbsoluteURLImpl urlimpl = (AbsoluteURLImpl) url;
            uri = u.resolve(urlimpl.u);
            if (uri == urlimpl.u) {
                return url;
            }
    	} else if (url instanceof RelativeURLImpl) {
        	// if relative: encode url path only or all string
    		// depending on local scheme or not
            String encodedUrl = (URLHelper.startsWithLocalScheme(getString()))
                ? ((RelativeURLImpl)url).getEncodedPathOnly()
                : URLEncoder.encode(url.getString());
            if (System.getProperty("os.name").startsWith("Windows") &&
                    encodedUrl.length()>1 && encodedUrl.charAt(1)==':') {
                encodedUrl = "/"+encodedUrl;
            }
    		uri = u.resolve(encodedUrl);
    	} else {
    		throw new NoSuccessException("Unknown class: " + url.getClass().getName());
    	}
        try {
			return new AbsoluteURLImpl(uri);
		} catch (BadParameterException e) {
			throw new NoSuccessException(e);
		}
    }

    public boolean isAbsolute() {
        return true;
    }

    public URL normalize() {
        URI uri = u.normalize();
        if (uri == u) {
            return this;
        }
        try {
			return new AbsoluteURLImpl(uri);
		} catch (BadParameterException e) {
			return this;
		}
    }

    ////////////////////////////////////////// java methods ///////////////////////////////////////////

    public int hashCode() {
        return u.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof AbsoluteURLImpl)) {
            return false;
        }
        AbsoluteURLImpl other = (AbsoluteURLImpl) o;
        return u.equals(other.u);
    }

    private URI getSchemeSpecificPart() {
        try {
            return new URI(u.getRawSchemeSpecificPart());
        } catch (URISyntaxException e) {
            return u;
        }
    }

}
