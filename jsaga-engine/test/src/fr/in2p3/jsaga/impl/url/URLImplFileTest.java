package fr.in2p3.jsaga.impl.url;

import org.junit.Ignore;
import org.junit.Test;
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
public class URLImplFileTest extends URLImplTest {
    
	protected static void init() {
    	URLImplTest.init();
	   	 _uri = "file:";
		 _user = "";
		 _host = "";
		 _port = "";
    	 _path_not_encoded = "/path with# spaces";
    	 _path_encoded = "/path%20with%23%20spaces";
		 _file = "Music &#39;erFraeFrance byAranyZoltn.mp3:1011";
    	 _query = "";
    	 _fragment = "";
     }

	@Test @Override @Ignore("to skip")
    public void test_redondantslashes()  throws Exception {
    }

	@Test
    public void test_path() throws Exception {
        URL url;
        
        url = URLFactory.createURL(_uri+_abs_path+_file);
        assertEquals(_abs_path+_file, url.getPath());

        url = URLFactory.createURL(_uri+"/");
        url.setPath(_abs_path+_file);
        assertEquals(_abs_path+_file, url.getPath());
    
    }

	@Test
    public void test_isabsolute() throws Exception {
    	URL url = URLFactory.createURL(_url_simple);
    	assertTrue(url.isAbsolute());
    	url = URLFactory.createURL(_url_relative);
    	assertFalse(url.isAbsolute());
    	url = URLFactory.createURL(_file);
    	assertFalse(url.isAbsolute());
    }
    
}
