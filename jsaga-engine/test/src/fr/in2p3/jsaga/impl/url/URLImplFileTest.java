package fr.in2p3.jsaga.impl.url;

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
    
    protected void init() {
    	super.init();
	   	 _uri = "file:";
		 _user = "";
		 _host = "";
		 _port = "";
    	 _query = "";
    	 _fragment = "";
     }

    /*
    public void test_remove() throws Exception {
        URL url;
        
        url = URLFactory.createURL(_url_simple);
        url.setString();
        assertEquals("", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setScheme();
        assertEquals(_user+_host+_port+_abs_path+_query+_fragment, url.getString());

    }
    public void test_replace() throws Exception {
        URL url;

        url = URLFactory.createURL(_url_simple);
        url.setString("NEW:///");
        assertEquals("NEW:///", url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setScheme("NEW");
        assertEquals("NEW://" + _user+_host+_port+_abs_path+_query+_fragment, url.getString());

        url = URLFactory.createURL(_url_simple);
        url.setPath("/f:/NEW");
        assertEquals(_uri+_user+_host+_port+"/f:/NEW"+_query+_fragment, url.getString());

    }
*/
}
