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
		 _file = "Music &#39;erFraeFr ancebyAranyZoltán.mp3";
    	 _query = "";
    	 _fragment = "";
     }

    public void test_redondantslashes()  throws Exception {
        super.ignore("To skip");
    }
}
