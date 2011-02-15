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
    
	public URLImplFileTest() throws Exception {
		super();
	}

	protected void init() {
    	super.init();
	   	 _uri = "file:";
		 _user = "";
		 _host = "";
		 _port = "";
		 _file = "contains#init";
    	 _query = "";
    	 _fragment = "";
     }

    public void test_redondantslashes()  throws Exception {
        URL url = URLFactory.createURL("//path/subdir");
        // path should not be considered as HOST
        assertTrue(url.getHost() == null);
    }

}
