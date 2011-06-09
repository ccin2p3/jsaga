package fr.in2p3.jsaga.adaptor.bes.security;

import fr.in2p3.jsaga.adaptor.security.JKSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJKSSecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 Nov 2010
* ***************************************************/

public class BesJKSSecurityAdaptor extends JKSSecurityAdaptor {

	// TODO : insert this code into JKSSecurityAdaptor ?
	
	private static final String KEYSTORE = "Keystore";
	private static final String KEYSTORE_PASS = "KeystorePass";
	
    public String getType() {
    	return "BESJKS";
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
    		String keyStorePass = (String) attributes.get(KEYSTORE_PASS);
			String keyStorePath = (String) attributes.get(KEYSTORE);
    		System.setProperty("javax.net.ssl.keyStore", keyStorePath);
    		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePass);
    		System.setProperty("javax.net.ssl.trustStore", keyStorePath);
    		//System.setProperty("javax.net.ssl.trustStorePassword", keyStorePass);
    		//System.setProperty("javax.net.debug", "ssl:handshake");
    		return super.createSecurityCredential(usage, attributes, contextId);
    }
}
