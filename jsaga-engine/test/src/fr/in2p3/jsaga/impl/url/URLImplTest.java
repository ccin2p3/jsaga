package fr.in2p3.jsaga.impl.url;

import junit.framework.TestCase;

import org.ogf.saga.error.BadParameterException;
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
public class URLImplTest extends TestCase {
	protected String _uri;
	protected String _user;
	protected String _host;
	protected String _port;
	protected String _abs_path;
	protected String _rel_path;
	protected String _path_with_space;
	protected String _dir;
	protected String _query;
	protected String _fragment;
	protected String _non_normalized_path_base;
	protected String _url_simple;
	protected String _url_relative;
	protected String _url_directory;
	protected String _url_space;
	protected String _url_non_normalized;
	
    public URLImplTest() {
    	super();
    	init();
    	_url_simple = _uri+_user+_host+_port+_abs_path+_query+_fragment; 	//"uri://userinfo@host.domain:1234/path?query=value#fragment"
    	_url_relative = _rel_path+_query+_fragment; 						//"path?query=value#fragment"
    	_url_directory = _uri+_user+_host+_port+_dir; 						//"uri://userinfo@host.domain:1234/dir/"
    	_url_space = _uri+_host+_port+_path_with_space; 					//"uri://host.domain:1234/path with spaces"
    	_url_non_normalized = _uri+_user+_host+_port+_non_normalized_path_base+_rel_path+_query+_fragment;
    																		//"uri://host.domain:1234/dir1/.././path"
    }
    
    protected void init() {
    	 _uri = "uri://";
    	 _user = "userinfo@";
    	 _host = "host.domain";
    	 _port = ":1234";
    	 _abs_path = "/path";
    	 _rel_path = "path";
    	 _path_with_space = "/path with spaces";
    	 _dir = "/dir/";
    	 _query = "?query=value";
    	 _fragment = "#fragment";
    	 _non_normalized_path_base = "/dir1/.././";
    }

    public void test_remove() throws Exception {
        URL url;

        /*java.net.URI u = new java.net.URI("file:///");
        System.out.println(u.toString());
        java.net.URI u2 = new java.net.URI("file", null, null, null, null);
        System.out.println(u2.toString());*/
        
        url = URLFactory.createURL(_url_simple);
        url.setString();
        assertEquals("", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setScheme();
        assertEquals(_user+_host+_port+_abs_path+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setUserInfo();
        assertEquals(_uri+_host+_port+_abs_path+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setUserInfo();
        url.setPort();
        url.setHost();
        // set null authority; the // is automatically removed by URI
        if (_uri.endsWith("//")) {
        	assertEquals(_uri.replaceAll("//","")+_abs_path+_query+_fragment, url.getString());
        } else {
        	assertEquals(_uri+_abs_path+_query+_fragment, url.getString());
        }

        url = URLFactory.createURL(_url_simple);
        url.setPort();
        assertEquals(_uri+_user+_host+_abs_path+_query+_fragment, url.getString());

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
        assertEquals(_uri+_user+_host+_port+_abs_path+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setFragment();
        assertEquals(_uri+_user+_host+_port+_abs_path+_query, url.getString());
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
        	assertEquals("NEW://" + _user+_host+_port+_abs_path+_query+_fragment, url.getString());
        } else {
        	assertEquals("NEW:" + _user+_host+_port+_abs_path+_query+_fragment, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setUserInfo("NEW");
        // If host was null, changing user does not change anything
        if (_host != "") {
	        assertEquals(_uri+"NEW@"+_host+_port+_abs_path+_query+_fragment, url.getString());
        } else {
        	assertEquals(_url_simple, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setHost("NEW");
        // If host was null, // is added by URI
        if (_host == "") {
        	assertEquals(_uri+"//"+_user+"NEW"+_port+_abs_path+_query+_fragment, url.getString());
        } else {
        	assertEquals(_uri+_user+"NEW"+_port+_abs_path+_query+_fragment, url.getString());
        }
        
        url = URLFactory.createURL(_url_simple);
        url.setPort(5678);
        // If host was null, chaning port does not change anything
        if (_host != "") {
        	assertEquals(_uri+_user+_host+":5678"+_abs_path+_query+_fragment, url.getString());
        } else {
        	assertEquals(_url_simple, url.getString());
        }

        url = URLFactory.createURL(_url_simple);
        url.setPath("/NEW");
        assertEquals(_uri+_user+_host+_port+"/NEW"+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setQuery("NEW=new");
        assertEquals(_uri+_user+_host+_port+_abs_path+"?NEW=new"+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setFragment("NEW");
        assertEquals(_uri+_user+_host+_port+_abs_path+_query+"#NEW", url.getString());
    }

    public void test_getString() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_uri+_host+_port+_path_with_space, url.getString());
    }

    public void test_getEscaped() throws Exception {
        URL url = URLFactory.createURL(_url_space);
        assertEquals(_uri+_host+_port+_path_with_space.replaceAll(" ", "%20"), url.getEscaped());
    }
    
    public void test_translate() throws Exception {
    	URL url = URLFactory.createURL(_url_simple);
    	URL translated = url.translate("NEW");
        // If host is null, the // is not printed
        if (_host != "") {
        	assertEquals("NEW://" + _user+_host+_port+_abs_path+_query+_fragment, translated.getString());
        } else {
        	assertEquals("NEW:" + _user+_host+_port+_abs_path+_query+_fragment, translated.getString());
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
    }
    
    public void test_resolve() throws Exception {
    	//"uri://userinfo@host:1234/path?query=value#fragment";
    	URL url = URLFactory.createURL(_url_simple);
    	String newAP = "NEW://NEWuser@NEWhost:9999";
    	URL resolved = url.resolve(URLFactory.createURL(newAP));
    	assertEquals(newAP, resolved.getString());
    	
    	String newFragment = "#NEWfragment";
    	resolved = url.resolve(URLFactory.createURL(newFragment));
    	assertEquals(_uri+_user+_host+_port+_abs_path+_query+newFragment, resolved.getString());
    	
    	url = URLFactory.createURL(_url_directory);
    	resolved = url.resolve(URLFactory.createURL(_url_relative));
    	assertEquals(_url_directory + _url_relative, resolved.getString());
    }
}
