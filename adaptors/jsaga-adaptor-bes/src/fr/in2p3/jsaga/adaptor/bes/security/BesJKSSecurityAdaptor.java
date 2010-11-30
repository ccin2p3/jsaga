package fr.in2p3.jsaga.adaptor.bes.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.JKSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
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

	private static final String KEYSTORE = "Keystore";
	private static final String KEYSTORE_PASS = "KeystorePass";
	
    public String getType() {
    	return "BESJKS";
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
    	
    		String keyStorePass = (String) attributes.get(KEYSTORE_PASS);
			String keyStorePath = (String) attributes.get(KEYSTORE);
    		System.setProperty("javax.net.ssl.keyStore", keyStorePath);
    		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePass); //"the!user");
    		System.setProperty("javax.net.ssl.trustStore", keyStorePath); //"/home/schwarz/.jsaga/contexts/unicore6/demouser.jks");
    		//System.setProperty("javax.net.debug", "SSL");
    		return super.createSecurityCredential(usage, attributes, contextId);
    }
}
