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
    	 _abs_path = "/c:/path";
    	 _rel_path = "path";
    	 _path_with_space = "/c:/path with space";
    	 _dir = "/c:/dir/";
    	 _non_normalized_path_base = "/c:/dir1/.././";
     }

}
