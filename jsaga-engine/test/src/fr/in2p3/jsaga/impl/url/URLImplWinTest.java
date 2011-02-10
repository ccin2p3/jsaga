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
public class URLImplWinTest extends URLImplFileTest {
    
    protected void init() {
    	super.init();
    	 _abs_path = "/c:/path/";
    	 _path_with_space = "/c:/path with space";
    }

    public void test_antislash()  throws Exception {
    	
        URL url = URLFactory.createURL("c:\\path");
        // backslash should be changed in slash
        assertEquals("c:/path", url.getString());
    }
}
