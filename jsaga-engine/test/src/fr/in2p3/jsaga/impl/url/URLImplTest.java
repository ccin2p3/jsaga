package fr.in2p3.jsaga.impl.url;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   URLImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class URLImplTest extends Assert {

    protected static String _uri;
    protected static String _user;
    protected static String _host;
    protected static String _port;
    protected static String _abs_path;
    protected static String _file;
    protected static String _rel_path;
    protected static String _path_not_encoded;
    protected static String _path_encoded;
    protected static String _query;
    protected static String _fragment;
    protected static String _non_normalized_path;
    protected static String _url_simple;
    protected static String _url_relative;
    protected static String _url_directory;
    protected static String _url_space;
    protected static String _url_non_normalized;
    protected static String _url_rel_non_normalized;
    
    @BeforeClass
    public static void setUp() throws Exception {
        init();
        _url_simple = _uri+_user+_host+_port+_abs_path+_file+_query+_fragment;     //"uri://userinfo@host.domain:1234/path?query=value#fragment"
        _url_relative = _rel_path+_file+_query+_fragment;                         //"path?query=value#fragment"
        _url_directory = _uri+_user+_host+_port+_abs_path;                         //"uri://userinfo@host.domain:1234/dir/"
        _url_space = _uri+_host+_port+_path_not_encoded;                     //"uri://host.domain:1234/path with spaces"
        _url_non_normalized = _uri+_user+_host+_port+_abs_path+_non_normalized_path+_file+_query+_fragment;
                                                                            //"uri://host.domain:1234/dir1/.././path"
        _url_rel_non_normalized = _rel_path+_non_normalized_path+_file;
    }
    
    protected static void init() {
         _uri = "uri://";
         _user = "userinfo@";
         _host = "host.domain";
         _port = ":1234";
         _file = "file.ext:1";
         _abs_path = "/path/";
         _rel_path = "relpath:suite/";
         _path_not_encoded = "/path with spaces";
         _path_encoded = "/path%20with%20spaces";
         //_dir = "/dir/";
         _query = "?query=value";
         _fragment = "#fragment";
         _non_normalized_path = "/dummy/.././";
    }

    @Test
    public void test_remove() throws Exception {
        URL url;

        url = URLFactory.createURL(_url_simple);
        url.setString();
        assertEquals("", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setScheme();
        assertEquals(_user+_host+_port+_abs_path+_file+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setUserInfo();
        assertEquals(_uri+_host+_port+_abs_path+_file+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setUserInfo();
        url.setPort();
        url.setHost();
        // set null authority; the // is automatically removed by URI
        if (_uri.endsWith("//")) {
            assertEquals(_uri.replaceAll("//","")+_abs_path+_file+_query+_fragment, url.getString());
        } else {
            assertEquals(_uri+_abs_path+_file+_query+_fragment, url.getString());
        }

        url = URLFactory.createURL(_url_simple);
        url.setPort();
        assertEquals(_uri+_user+_host+_abs_path+_file+_query+_fragment, url.getString());

        // set Path to null, impossible if host is null
        url = URLFactory.createURL(_url_simple);
        try {
            url.setPath();
            assertEquals(_uri+_user+_host+_port+_query+_fragment, url.getString());
        } catch (BadParameterException bpe) {
            if (url.getHost() != null) throw bpe;
        }

        url = URLFactory.createURL(_url_simple);
        url.setQuery();
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setFragment();
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+_query, url.getString());
    }

    @Test
    public void test_replace() throws Exception {
        URL url;

        url = URLFactory.createURL(_url_simple);
        url.setString("NEW:///");
        assertEquals("NEW:/", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setString("NEW:/");
        assertEquals("NEW:/", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setScheme("NEW");
        // If host is null, the // is not printed
        if (_host != "") {
            assertEquals("NEW://" + _user+_host+_port+_abs_path+_file+_query+_fragment, url.getString());
        } else {
            assertEquals("NEW:" + _user+_host+_port+_abs_path+_file+_query+_fragment, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setUserInfo("NEW");
        // If host was null, changing user does not change anything
        if (_host != "") {
            assertEquals(_uri+"NEW@"+_host+_port+_abs_path+_file+_query+_fragment, url.getString());
        } else {
            assertEquals(_url_simple, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setHost("NEW");
        // If host was null, // is added by URI
        if (_host == "") {
            assertEquals(_uri+"//"+_user+"NEW"+_port+_abs_path+_file+_query+_fragment, url.getString());
        } else {
            assertEquals(_uri+_user+"NEW"+_port+_abs_path+_file+_query+_fragment, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setPort(5678);
        // If host was null, changing port does not change anything
        if (_host != "") {
            assertEquals(_uri+_user+_host+":5678"+_abs_path+_file+_query+_fragment, url.getString());
        } else {
            assertEquals(_url_simple, url.getString());
        }

        url = URLFactory.createURL(_url_simple);
        url.setPath("/NEW");
        assertEquals(_uri+_user+_host+_port+"/NEW"+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setQuery("NEW=new");
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+"?NEW=new"+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setFragment("NEW");
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+_query+"#NEW", url.getString());
        
        // change path to a directory
        url = URLFactory.createURL(_uri+_user+_host+_port+_abs_path+_file);
        url.setPath(url.getPath() + "/");
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+"/", url.getString());
        
    }

    @Test
    public void test_relative() throws Exception {
        URL url;
        url = URLFactory.createURL(_abs_path);
        assertTrue(url instanceof RelativeURLImpl);
        assertNull(url.getScheme());
        assertEquals(-1, url.getPort());
        assertNull(url.getHost());
        assertNull(url.getUserInfo());
        try {
            url.setPort(8080);
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        try {
            url.setScheme("dummy");
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        try {
            url.setHost("dummy");
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        try {
            url.setUserInfo("dummy");
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        url.setQuery("NEW=new");
        assertEquals(_abs_path+"?NEW=new", url.getString());
        url.setFragment("NEW");
        assertEquals(_abs_path+"?NEW=new#NEW", url.getString());
        url.setString("dummy");
        try {
            assertNull(url.getQuery());
        } catch (RuntimeException re) {
        }
        try {
            assertNull(url.getFragment());
        } catch (RuntimeException re) {
        }
        
        url = URLFactory.createURL(_abs_path);
        url.setFragment("NEW");
        assertEquals(_abs_path+"#NEW", url.getString());

        url = URLFactory.createURL(_url_simple);
        try {
            url.setString(_url_relative);
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        
        url = URLFactory.createURL(_url_relative);
        try {
            url.setString(_url_simple);
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
        
    }
    
    @Test
    public void test_getString() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_url_space, url.getString());
    }

    @Test
    public void test_getEscaped() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_uri+_host+_port+_path_encoded, url.getEscaped());
        
        url = URLFactory.createURL(_url_simple);
        url.setPath(_path_not_encoded);
        assertEquals(_uri+_user+_host+_port+_path_encoded+_query+_fragment, url.getEscaped());
        
        url = URLFactory.createURL(_abs_path);
        assertNull(url.getEscaped());

    }
    
    @Test
    public void test_translate() throws Exception {
        URL url = URLFactory.createURL(_url_simple);
        URL translated = url.translate("NEW");
        // If host is null, the // is not printed
        if (_host != "") {
            assertEquals("NEW://" + _user+_host+_port+_abs_path+_file+_query+_fragment, translated.getString());
        } else {
            assertEquals("NEW:" + _user+_host+_port+_abs_path+_file+_query+_fragment, translated.getString());
        }
        
        
        url = URLFactory.createURL(_abs_path);
        try {
            url.translate("NEW");
            fail("Excepted exception: " + BadParameterException.class);
        } catch (BadParameterException bpe) {
        }
    }
    
    @Test
    public void test_isabsolute() throws Exception {
        URL url = URLFactory.createURL(_url_simple);
        assertTrue(url.isAbsolute());
        url = URLFactory.createURL(_url_relative);
        assertFalse(url.isAbsolute());
    }
    
    @Test
    public void test_normalize() throws Exception {
        URL url = URLFactory.createURL(_url_non_normalized);
        URL normalized = url.normalize();
        assertEquals(_url_simple, normalized.getString());
        
        url = URLFactory.createURL(_rel_path+_non_normalized_path+_file);
        url.setQuery("query=value");
        url.setFragment("fragment");
        normalized = url.normalize();
        assertEquals(_rel_path+_file+"?query=value#fragment", normalized.getString());

        url = URLFactory.createURL(_abs_path+_non_normalized_path+_file);
        url.setFragment("fragment");
        normalized = url.normalize();
        assertEquals(_abs_path+_file+"#fragment", normalized.getString());

    }
    
    @Test
    public void test_resolve() throws Exception {
        URL url, newURL;
        URL resolved;
        String newFragment = "NEWfragment";
        String newQuery = "query=value";
        
        // resolve ABS against ABS
        //"uri://userinfo@host:1234/path?query=value#fragment";
        url = URLFactory.createURL(_url_simple);
        String newAP = "NEW://NEWuser@NEWhost:9999";
        resolved = url.resolve(URLFactory.createURL(newAP));
        assertEquals(newAP, resolved.getString());
        
        // resolve REL with path against ABS
        url = URLFactory.createURL(_url_simple);
        resolved = url.resolve(URLFactory.createURL(_url_relative));
        assertEquals(_uri+_user+_host+_port+_abs_path+_url_relative, resolved.getString());
        
        // encoding
        url = URLFactory.createURL(_url_simple);
        resolved = url.resolve(URLFactory.createURL(_path_not_encoded));
        assertEquals(_uri+_user+_host+_port+_path_not_encoded,resolved.getString());
        assertEquals(_uri+_user+_host+_port+_path_encoded,resolved.toString());
        
        // resolve REL without path and with fragment against ABS
        url = URLFactory.createURL(_url_simple);
        newURL = URLFactory.createURL("");
        newURL.setFragment(newFragment);
        resolved = url.resolve(newURL);
        assertEquals(_uri+_user+_host+_port+_abs_path+_file+_query+"#"+newFragment, resolved.getString());
        
        // resolve REL against REL
        url = URLFactory.createURL(_rel_path);
        resolved = url.resolve(URLFactory.createURL(_file));
        assertEquals(_rel_path+_file,resolved.getString());
        
        url = URLFactory.createURL(_rel_path);
        url.setQuery("OLDquery=OLDvalue");
        newURL = URLFactory.createURL(_file);
        newURL.setQuery(newQuery);
        newURL.setFragment(newFragment);
        resolved = url.resolve(newURL);
        assertEquals(_rel_path+_file+"?"+newQuery+"#"+newFragment,resolved.getString());
        
        url = URLFactory.createURL(_rel_path+"file.txt");
        newURL = URLFactory.createURL(_file);
        newURL.setQuery(newQuery);
        resolved = url.resolve(newURL);
        assertEquals(_rel_path+_file+"?"+newQuery,resolved.getString());
        
        // resolve ABS against REL: impossible
        url = URLFactory.createURL(_url_relative);
        try {
            resolved = url.resolve(URLFactory.createURL(_url_simple));
            fail("Excepted exception: " + NoSuccessException.class);
        } catch (NoSuccessException nse) {
        }
        
        url = URLFactory.createURL(_abs_path);
        assertEquals(url, url.resolve(url));
    }
    
    @Test
    public void test_helpers() throws Exception {
        URL baseUrl;
        URL relativeUrl;
        URL url;
        
        baseUrl = URLFactory.createURL(_url_directory);
        relativeUrl = URLFactory.createURL(_url_relative);
        url = URLHelper.createURL(baseUrl, relativeUrl);
        assertEquals(_uri+_user+_host+_port+_abs_path+_url_relative, url.getString());
        
        baseUrl = URLFactory.createURL(_url_directory);
        url = URLHelper.createURL(baseUrl, _url_relative);
        assertEquals(_uri+_user+_host+_port+_abs_path+_url_relative, url.getString());
        
        baseUrl = URLFactory.createURL(_url_directory+"FILE");
        url = URLHelper.getParentURL(baseUrl);
        assertEquals(_uri+_user+_host+_port+_abs_path, url.getString());
    }

    @Test
    public void test_redondantslashes()  throws Exception {
        URL url;

        url = URLFactory.createURL(_uri+_user+_host+_port+"////"+_abs_path+_file);
        // path should not be considered as HOST
        assertEquals(_abs_path+_file, url.getPath());

        url = URLFactory.createURL(_uri+_user+_host+_port);
        url.setPath("////"+_abs_path+_file);
        // path should not be considered as HOST
        assertEquals(_abs_path+_file, url.getPath());

        url = URLFactory.createURL("DUMMY:///");
        url.setString(_uri+_user+_host+_port+"////"+_abs_path+_file);
        // path should not be considered as HOST
        assertEquals(_abs_path+_file, url.getPath());
    }
    
    @Test
    public void test_cache() throws Exception {
        AbstractURLImpl url;
        url = new RelativeURLImpl(new URLAttributes(_url_relative));
        assertTrue(url.hasCache());
        url = new AbsoluteURLImpl(new URLAttributes(_url_simple));
        assertTrue(url.hasCache());

        String oldLifeTime = EngineProperties.getProperty(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME);
        Boolean hasCache;
        
        EngineProperties.setProperty(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME, "0");
        hasCache = new RelativeURLImpl(new URLAttributes(_url_relative)).hasCache();
        EngineProperties.setProperty(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME, oldLifeTime);
        assertFalse(hasCache);

        EngineProperties.setProperty(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME, "0");
        hasCache = new AbsoluteURLImpl(new URLAttributes(_url_simple)).hasCache();
        EngineProperties.setProperty(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME, oldLifeTime);
        assertFalse(hasCache);
    }

    private class URLAttributes extends FileAttributes {
        private String m_name;
        
        public URLAttributes(String name) {
            m_name = name;
        }
        public String getName() {
            return m_name;
        }
        public int getType() {
            return TYPE_UNKNOWN;
        }

        public long getSize() {
            return SIZE_UNKNOWN;
        }

        public PermissionBytes getUserPermission() {
            return PERMISSION_UNKNOWN;
        }

        public PermissionBytes getGroupPermission() {
            return PERMISSION_UNKNOWN;
        }

        public PermissionBytes getAnyPermission() {
            return PERMISSION_UNKNOWN;
        }

        public String getOwner() {
            return ID_UNKNOWN;
        }

        public String getGroup() {
            return ID_UNKNOWN;
        }

        public long getLastModified() {
            return DATE_UNKNOWN;
        }
    }

}
