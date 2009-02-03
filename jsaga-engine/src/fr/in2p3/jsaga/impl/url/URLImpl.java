package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.url.URL;

import java.net.URI;
import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLImpl implements URL {
    private URI u;
    private FileAttributes m_cache;
    private boolean m_mustRemoveSlash;

    /** MAY encode the URL */
    URLImpl(String url, boolean encode) throws BadParameterException {
        if (encode) {
            if (url.startsWith("file://")) {
                url = URLEncoder.encodePathOnly(url);
            } else {
                url = URLEncoder.encode(url);
            }
        }
        try {
            u = new URI(url);
            this.fixFileURI();  //sreynaud
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in url", e);
        }
    }

    /** Encode the relative path */
    URLImpl(String relativePath) throws BadParameterException {
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

    /** Encode the relative path */
    URLImpl(URL base, String relativePath) throws BadParameterException {
        this(   base,
                URLEncoder.encodePathOnly(relativePath),
                null,
                null
        );
    }

    /** Encode the URL */
    URLImpl(URL base, URL relativeUrl) throws BadParameterException {
        this(   base,
                URLEncoder.encodePathOnly(relativeUrl.getPath()),
                relativeUrl.getQuery()!=null ? relativeUrl.getQuery() : base.getQuery(),
                relativeUrl.getFragment()!=null ? relativeUrl.getFragment() : base.getFragment()
        );
    }

    /** DO NOT encode the URL */
    private URLImpl(URL base, String relativePath, String query, String fragment) {
        //workaround: Windows absolute paths must start with one and only one '/'
        if (isWindowsAbsolutePath(base.getScheme(), relativePath)) {
            relativePath = "/"+relativePath;
        }

        // remove redondant '/'
        if (relativePath.startsWith("//")) {
            int i;for(i=0; i<relativePath.length() && relativePath.charAt(i)=='/'; i++);
            if(i>1)relativePath="/"+relativePath.substring(i);
        }

        // resolve URI
        URI baseUri = ((URLImpl) base).u;
        String relativeUri = relativePath
                + concatIfNotNull('?', new String[]{query, baseUri.getQuery()})
                + concatIfNotNull('#', new String[]{fragment, baseUri.getFragment()});
        u = baseUri.resolve(relativeUri);
    }

    /** DO NOT encode the URL */
    private URLImpl(URI u) {
        this.u = u;
    }

    /** Encode the URL */
    public void setString(String url) throws BadParameterException {
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

    /** Decode the URL */
    public String getString() {
        return URLEncoder.decode(u, m_mustRemoveSlash);
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
        } else if (".".equals(u.getAuthority())) {
            return "."+u.getPath();                         //sreynaud
        } else if (u.getPath().startsWith("/./") || m_mustRemoveSlash) {
            return u.getPath().substring(1);                //sreynaud
        }
        return u.getPath();
    }

    public void setPath(String path) throws BadParameterException {
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
            return new URLImpl(url);
        } catch(URISyntaxException e) {
            throw new BadParameterException("syntax error in scheme", e);
        }
    }

    public URL resolve(URL url) throws NoSuccessException {
        URLImpl urlimpl = (URLImpl) url;
        URI uri = u.resolve(urlimpl.u);
        if (uri == urlimpl.u) {
            return url;
        }
        return new URLImpl(uri);
    }

    public boolean isAbsolute() {
        return u.isAbsolute();
    }

    public URL normalize() {
        URI uri = u.normalize();
        if (uri == u) {
            return this;
        }
        return new URLImpl(uri);
    }

    ////////////////////////////////////////// java methods ///////////////////////////////////////////

    public int hashCode() {
        return u.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof URLImpl)) {
            return false;
        }
        URLImpl other = (URLImpl) o;
        return u.equals(other.u);
    }

    /** DO NOT decode the URL */
    public String toString() {
        return u.toString();
    }

    ////////////////////////////////////////// cache methods //////////////////////////////////////////

    public void setCache(FileAttributes cache) {
        m_cache = cache;
    }

    public FileAttributes getCache() {
        return m_cache;
    }

    public boolean hasCache() {
        return (m_cache != null);
    }

    ///////////////////////////////////////// private methods /////////////////////////////////////////

    private boolean isWindowsAbsolutePath(String scheme, String relativePath) {
        return ("file".equals(scheme) || "zip".equals(scheme))
                && System.getProperty("os.name").startsWith("Windows")
                && relativePath.length()>=2 && Character.isLetter(relativePath.charAt(0)) && relativePath.charAt(1)==':'
                && (relativePath.length()==2 || (relativePath.length()>2 && relativePath.charAt(2)=='/'));
    }

    private void fixFileURI() throws URISyntaxException {
        boolean isRelative = (u.getHost()==null && u.getAuthority()!=null && !u.getAuthority().equals("."));
        boolean isWindows = (u.getHost()!=null && u.getHost().length()==1 && u.getAuthority()!=null && u.getAuthority().endsWith(":"));
        if (isRelative || isWindows) {
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
