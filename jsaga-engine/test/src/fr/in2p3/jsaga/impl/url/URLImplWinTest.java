package fr.in2p3.jsaga.impl.url;

import org.ogf.saga.error.BadParameterException;
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
public class URLImplWinTest extends URLImplFileTest {
    
    protected void init() {
    	super.init();
    	 _abs_path = "/c:/path/";
    	 _path_not_encoded = "/c:/path with#and?and{}and%and\"and[]";
    	 _path_encoded = "/c:/path%20with%23and%3Fand%7B%7Dand%25and%22and%5B%5D";
    }

    public void test_antislash()  throws Exception {
    	
    	// Simulate Windows
    	System.setProperty("file.separator", "\\");
        URL url = URLFactory.createURL("c:\\path");
        // backslash should be changed in slash
        assertEquals("c:/path", url.getString());
    }
    
    public void test_replace() throws Exception {
    	super.test_replace();
        URL url = URLFactory.createURL(_url_simple);
        String newWinFile = "e:/data/file.txt";
        url.setPath(newWinFile);
        // a / should be added
        assertEquals(_uri+_user+_host+_port+"/"+newWinFile+_query+_fragment, url.getString());
    }
    
    public void test_relative() throws Exception {
    	super.test_relative();
    	URL url;
    	
    	url = URLFactory.createURL(_url_simple);
    	try {
    		url.setString("c:/data/file.txt");
    		fail("BadParameterException was expected");
    	} catch (BadParameterException bpe) {
    	} catch (Exception e) {
    		fail("NoSuccessException was expected");
    	}
    	
 
    }
    
}
