package fr.in2p3.jsaga.impl.url;

import junit.framework.TestCase;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public class URLImplTest extends AbstractTest {

	protected String _uri;
	protected String _user;
	protected String _host;
	protected String _port;
	protected String _abs_path;
	protected String _file;
	protected String _rel_path;
	protected String _path_not_encoded;
	protected String _path_encoded;
	protected String _query;
	protected String _fragment;
	protected String _non_normalized_path;
	protected String _url_simple;
	protected String _url_relative;
	protected String _url_directory;
	protected String _url_space;
	protected String _url_non_normalized;
	protected String _url_rel_non_normalized;
	
	public URLImplTest() throws Exception {
		super();
	}

	public void setUp() throws Exception {
    	super.setUp();
    	init();
    	_url_simple = _uri+_user+_host+_port+_abs_path+_file+_query+_fragment; 	//"uri://userinfo@host.domain:1234/path?query=value#fragment"
    	_url_relative = _rel_path+_file+_query+_fragment; 						//"path?query=value#fragment"
    	_url_directory = _uri+_user+_host+_port+_abs_path; 						//"uri://userinfo@host.domain:1234/dir/"
    	_url_space = _uri+_host+_port+_path_not_encoded; 					//"uri://host.domain:1234/path with spaces"
    	_url_non_normalized = _uri+_user+_host+_port+_abs_path+_non_normalized_path+_file+_query+_fragment;
    																		//"uri://host.domain:1234/dir1/.././path"
    	_url_rel_non_normalized = _rel_path+_non_normalized_path+_file;
    }
    
    protected void init() {
    	 _uri = "uri://";
    	 _user = "userinfo@";
    	 _host = "host.domain";
    	 _port = ":1234";
    	 _file = "file:1";
    	 _abs_path = "/path/";
    	 _rel_path = "relpath/";
    	 _path_not_encoded = "/path with spaces";
    	 _path_encoded = "/path%20with%20spaces";
    	 //_dir = "/dir/";
    	 _query = "?query=value";
    	 _fragment = "#fragment";
    	 _non_normalized_path = "/dummy/.././";
    }

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

    public void test_relative() throws Exception {
    	URL url;
    	url = URLFactory.createURL(_abs_path);
    	assertTrue(url instanceof RelativeURLImpl);
    	url.setQuery("NEW=new");
    	assertEquals(_abs_path+"?NEW=new", url.getString());
    	url.setFragment("NEW");
    	assertEquals(_abs_path+"?NEW=new#NEW", url.getString());

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
    
    public void test_getString() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_url_space, url.getString());
    }

    public void test_getEscaped() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_uri+_host+_port+_path_encoded, url.getEscaped());
        
        url = URLFactory.createURL(_url_simple);
        url.setPath(_path_not_encoded);
        assertEquals(_uri+_user+_host+_port+_path_encoded+_query+_fragment, url.getEscaped());
    }
    
    public void test_translate() throws Exception {
    	URL url = URLFactory.createURL(_url_simple);
    	URL translated = url.translate("NEW");
        // If host is null, the // is not printed
        if (_host != "") {
        	assertEquals("NEW://" + _user+_host+_port+_abs_path+_file+_query+_fragment, translated.getString());
        } else {
        	assertEquals("NEW:" + _user+_host+_port+_abs_path+_file+_query+_fragment, translated.getString());
        }
    }
    
    public void test_isabsolute() throws Exception {
    	URL url = URLFactory.createURL(_url_simple);
    	assertTrue(url.isAbsolute());
    	url = URLFactory.createURL(_url_relative);
    	assertFalse(url.isAbsolute());
    }
    
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
    }
    
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

}
