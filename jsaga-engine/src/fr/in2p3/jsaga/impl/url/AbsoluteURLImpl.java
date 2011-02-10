package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.net.URI;
import java.net.URISyntaxException;

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
public class AbsoluteURLImpl extends AbstractSagaObjectImpl implements URL {
    protected URI u;
    //protected FileAttributes m_cache;
    private boolean m_mustRemoveSlash;

    /** MAY encode the URL */
    AbsoluteURLImpl(String url) throws BadParameterException {
    	this(url, true);
    }

    AbsoluteURLImpl(String url, boolean encode) throws BadParameterException {
        if (encode) {
            if (url.startsWith("file:/")) {
                url = URLEncoder.encodePathOnly(url);
            } else {
                url = URLEncoder.encode(url);
            }
        }
        try {
            /*LSZwhile (url.startsWith("//")) {
                url = url.substring(1); // prevent URI to consider root dir as a host
            }*/
            u = new URI(url);
            this.fixFileURI();  //sreynaud
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in url", e);
        }
    }

    /** Encode the relative path */
    /*
    AbsoluteURLImpl(String relativePath) throws BadParameterException {
        int colonPos = relativePath.indexOf(':');
        int slashPos = relativePath.indexOf('/');
        m_mustRemoveSlash = colonPos > -1 && (slashPos == -1 || colonPos < slashPos);
        if (m_mustRemoveSlash) {
            relativePath = URLEncoder.encodePathOnly("/"+relativePath);
        } else {
            relativePath = URLEncoder.encodePathOnly(relativePath);
        }
        try {
            u = new URI(relativePath);
        } catch (URISyntaxException e) {
            throw new BadParameterException("syntax error in url", e);
        }
    }
    */
    
    /** Encode the relative path + set the cache */
    /*
    AbsoluteURLImpl(FileAttributes cache) throws BadParameterException {
    	// TODO: check FileAttributes.getRelativePath()
        this(cache.getRelativePath());
        m_cache = cache;
    }
    */
    
    /** Encode the relative path */
    AbsoluteURLImpl(URL base, String relativePath) {
        this(   base,
                URLEncoder.encodePathOnly(relativePath),
                null,
                null
        );
    }

    /** Encode the URL */
    AbsoluteURLImpl(URL base, URL relativeUrl) {
    	// TODO : call resolve
        this(   base,
                URLEncoder.encodePathOnly(relativeUrl.getPath()),
                relativeUrl.getQuery()!=null ? relativeUrl.getQuery() : base.getQuery(),
                relativeUrl.getFragment()!=null ? relativeUrl.getFragment() : base.getFragment()
        );
    }

    /** DO NOT encode the URL */
    private AbsoluteURLImpl(URL base, String relativePath, String query, String fragment) {
    	
        //workaround: Windows absolute paths must start with one and only one '/'
        if (isWindowsAbsolutePath(base.getScheme(), relativePath)) {
            relativePath = "/"+relativePath;
        }

        // remove redondant '/'
        if (relativePath.startsWith("//")) {
            int i;for(i=0; i<relativePath.length() && relativePath.charAt(i)=='/'; i++);
            if(i>1)relativePath="/"+relativePath.substring(i);
        }

        //workaround: force to be interpreted as a relative path (even if path contains character ':')
        if (! relativePath.startsWith("/")) {
            relativePath = "./"+relativePath;
        }

    	// TODO: remove lines above and use AbsoluteURLImpl(URL base, URL relativeUrl) with RelativeURLImpl
        // resolve URI
        URI baseUri = ((AbsoluteURLImpl) base).u;
        String relativeUri = relativePath
                + concatIfNotNull('?', new String[]{query, baseUri.getQuery()})
                + concatIfNotNull('#', new String[]{fragment, baseUri.getFragment()});
        u = baseUri.resolve(relativeUri);
    }

    /** DO NOT encode the URL */
    protected AbsoluteURLImpl(URI u) {
        this.u = u;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbsoluteURLImpl clone = (AbsoluteURLImpl) super.clone();
        clone.u = u;
        clone.m_mustRemoveSlash = m_mustRemoveSlash;
        return clone;
    }

    /** Encode the URL */
    public void setString(String url) throws BadParameterException {
        if (url == null) {
            url = "";
        }
    	if (url.indexOf(":") >= 0 && url.indexOf(":") < 2) {
    		throw new BadParameterException("URL must be absolute");
    	}
        String encodedUrl = (url.startsWith("file://"))
                ? URLEncoder.encodePathOnly(url)
                : URLEncoder.encode(url);
        try {
            u = new URI(encodedUrl);
            this.fixFileURI();  //sreynaud
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in url", e);
        }
    }
    public void setString() throws BadParameterException {
        this.setString(null);
    }

    /** Decode the URL */
    public String getString() {
        return URLEncoder.decode(u, m_mustRemoveSlash);
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
    public void setFragment() throws BadParameterException {
        this.setFragment(null);
    }

    public String getHost() {
        if (u.getHost() == null) {
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
    public void setHost() throws BadParameterException {
        this.setHost(null);
    }

    public String getPath() {
        if (u.getPath() == null) {
            return this.getSchemeSpecificPart().getPath();  //sreynaud
        } else if (".".equals(u.getAuthority())) {
            return "."+u.getPath();                         //sreynaud
        } else if (u.getPath().startsWith("/./") || m_mustRemoveSlash) {
            return u.getPath().substring(1);                //sreynaud
        } else if (u.getPath().startsWith("//")) {
            return trimPath(u.getPath());                   //sreynaud
        }
        return u.getPath();
    }
    private static String trimPath(String path) {
        if (path.startsWith("//")) {
            return trimPath(path.substring(1));
        } else {
            return path;
        }
    }

    public void setPath(String path) throws BadParameterException {
        if (path == null) {
            path = "";
        }
        try {
            if (path.startsWith("./")) {
                //sreynaud: set relative path
                u = new URI(u.getScheme(), u.getAuthority(),
                        path.substring(2), u.getQuery(), u.getFragment());
            } else {
                //sreynaud: fix absolute path
                int i;for(i=0; i<path.length() && path.charAt(i)=='/'; i++);
                if(i>1)path="/"+path.substring(i);
                if (path == "" && u.getRawAuthority() == null) 
                	throw new BadParameterException("Path cannot by empty if authority is empty");
                u = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                        u.getPort(), path, u.getQuery(), u.getFragment());
            }
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in path", e);
        }
    }
    public void setPath() throws BadParameterException {
        this.setPath(null);
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
    public void setPort() throws BadParameterException {
        this.setPort(-1);
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
    public void setQuery() throws BadParameterException {
        this.setQuery(null);
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
    public void setScheme() throws BadParameterException {
        this.setScheme(null);
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
    public void setUserInfo() throws BadParameterException {
        this.setUserInfo(null);
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
    public URL translate(Session session, String scheme) throws BadParameterException, NoSuccessException {
        return this.translate(scheme);
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
        	// if relative: encode url string
    		String url_string = URLEncoder.encode(url.getString());
    		uri = u.resolve(url_string);
    	} else {
    		throw new NoSuccessException("Unknown class: " + url.getClass().getName());
    	}
        return new AbsoluteURLImpl(uri);
    }

    public boolean isAbsolute() {
        return true; //u.isAbsolute();
    }

    public URL normalize() {
        URI uri = u.normalize();
        if (uri == u) {
            return this;
        }
        return new AbsoluteURLImpl(uri);
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

    ///////////////////////////////////////// private methods /////////////////////////////////////////

    private boolean isWindowsAbsolutePath(String scheme, String relativePath) {
        return ("file".equals(scheme) || "zip".equals(scheme))
                && System.getProperty("os.name").startsWith("Windows")
                && relativePath.length()>=2 && Character.isLetter(relativePath.charAt(0)) && relativePath.charAt(1)==':'
                && (relativePath.length()==2 || (relativePath.length()>2 && relativePath.charAt(2)=='/'));
    }

    private void fixFileURI() throws URISyntaxException {
    	// FIXME : u.getAuthority().equals(".")
    	//LSZ relative URL are in RelativeURLImpl
        //boolean isRelative = (u.getHost()==null && u.getAuthority()!=null && !u.getAuthority().equals("."));
        boolean isWindows = (u.getHost()!=null && u.getHost().length()==1 && u.getAuthority()!=null && u.getAuthority().endsWith(":"));
        if (/*isRelative ||*/ isWindows) {
            u = new URI(u.getScheme(), u.getUserInfo(),
                    "",                                 // fix number of '/' after scheme
                    u.getPort(),
                    "/"+u.getAuthority()+u.getPath(),   // fix path
                    u.getQuery(), u.getFragment());
        }
    }

    private URI getSchemeSpecificPart() {
        try {
            return new URI(u.getRawSchemeSpecificPart());
        } catch (URISyntaxException e) {
            return u;
        }
    }

    private static String concatIfNotNull(char prefix, String[] suffix) {
        for (int i=0; suffix!=null && i<suffix.length; i++) {
            if (suffix[i] != null) {
                return prefix + suffix[i];
            }
        }
        return "";
    }
}
