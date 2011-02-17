package fr.in2p3.jsaga.impl.url;

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
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   1 fev 2011
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class URLImplWinTest extends AbstractTest {
    
	protected String _abs_path;
	protected String _file;
	protected String _rel_path;

	public URLImplWinTest() throws Exception {
		super();
	}

	public void setUp() throws Exception {
		super.setUp();
    	_abs_path = "C:/path/";
    	_rel_path = "relpath/";
        _file = "file with#and{}and%and[]";
        //_path_encoded = "c:/path%20with%23and%3Fand%7B%7Dand%25and%22and%5B%5D";
	}

    public void test_antislash()  throws Exception {
    	/*if (!System.getProperty("os.name").startsWith("Windows")) {
    		super.ignore("Not on Windows");
    		return;
    	}*/
        URL url = URLFactory.createURL("c:\\path");
        // backslash should be changed in slash
        assertEquals("c:/path", url.getString());
    }
    
    public void test_replace() throws Exception {
    	/*if (!System.getProperty("os.name").startsWith("Windows")) {
    		super.ignore("Not on Windows");
    		return;
    	}*/
        URL url;
        String newWinFile = "e:/data/file.txt";
        
        url = URLFactory.createURL(_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals(newWinFile, url.getString());
        
        url = URLFactory.createURL("file:/"+_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());

        url = URLFactory.createURL("file:///"+_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());

    
        url = URLFactory.createURL("file:/"+_abs_path+_file);
        url.setPath("////" + newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());
    }
    
    public void test_relative() throws Exception {
    	/*if (!System.getProperty("os.name").startsWith("Windows")) {
    		super.ignore("Not on Windows");
    		return;
    	}*/
    	URL url = URLFactory.createURL("uri://host/path");
    	try {
    		url.setString(_abs_path+_file);
    		fail("BadParameterException was expected");
    	} catch (BadParameterException bpe) {
    	} catch (Exception e) {
    		fail("NoSuccessException was expected");
    	}
 
    }
    
    public void test_specialChars() throws Exception {
        URL url = URLFactory.createURL(_file);
        // # and other chars should not be considered as special characters
        assertEquals(_file, url.getPath());
        
    }

    public void test_isabsolute() throws Exception {
    	URL url = URLFactory.createURL(_abs_path+_file);
    	assertFalse(url.isAbsolute());
    }

    public void test_normalize() throws Exception {
    	URL url, normalized;
    	
    	url = URLFactory.createURL(_abs_path + "./dummy/../" + _file);
    	normalized = url.normalize();
    	assertEquals(_abs_path+_file, normalized.getString());
    	
    	url = URLFactory.createURL(_rel_path + "./dummy/../" + _file);
    	normalized = url.normalize();
    	assertEquals(_rel_path+_file, normalized.getString());
    }
    
    public void test_resolve() throws Exception {
    	URL url;
    	URL resolved;
    	
    	url = URLFactory.createURL(_abs_path);
    	resolved = url.resolve(URLFactory.createURL(_file));
    	assertEquals(_abs_path+_file, resolved.getString());
    	
    	url = URLFactory.createURL(_rel_path);
    	resolved = url.resolve(URLFactory.createURL(_file));
    	assertEquals(_rel_path+_file,resolved.getString());
    	
    	url = URLFactory.createURL(_rel_path+"file.txt");
    	resolved = url.resolve(URLFactory.createURL(_file));
    	assertEquals(_rel_path+_file,resolved.getString());
    	
    }
    
    public void test_query_fragment() throws Exception {
    	URL url;
    	url = URLFactory.createURL(_abs_path+_file);
    	url.setQuery("query=value");
    	assertEquals("query=value", url.getQuery());
    	url = URLFactory.createURL(_abs_path+_file);
    	url.setFragment("fragment");
    	assertEquals("fragment", url.getFragment());
    }
    
}
