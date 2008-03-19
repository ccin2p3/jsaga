package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityAdaptor;


import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JKSSecurityAdaptorBuilder
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 mars 2008
* ***************************************************/

public class JKSSecurityAdaptorBuilder implements SecurityAdaptorBuilder {

	private static final String KEYSTORE = "Keystore";
	private static final String KEYSTORE_PASS = "KeystorePass";
	private static final String USER_ALIAS = "UserAlias";
	
    public String getType() {
    	return "JKS";
    }

    public Class getSecurityAdaptorClass() {
        return JKSSecurityAdaptor.class;
    }

    public Usage getUsage() {
    	return new UAnd(
    			 new Usage[]{
    					 new UFile(KEYSTORE), 
    					 new U(KEYSTORE_PASS),
    					 new UOptional(USER_ALIAS),
    					 new UOptional(Context.USERPASS)});
    }

    public Default[] getDefaults(Map map) throws IncorrectState {
        return new Default[]{
        		 new Default(KEYSTORE, new File[]{
                         new File(System.getProperty("user.home")+"/.keystore")}),
        };
    }

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectState, NoSuccess {
    	
    	try {
    		// get required attributes
    		String keyStorePass = (String) attributes.get(KEYSTORE_PASS);
			String keyStorePath = (String) attributes.get(KEYSTORE);

			// get optional attributes
			String userPass = (String) attributes.get(KEYSTORE_PASS);
			if (attributes.containsKey(Context.USERPASS)) {
				userPass = (String) attributes.get(Context.USERPASS);
			}
			
			// load the keystore
			KeyStore keyStore = KeyStore.getInstance("jks");
	        File f = new File(keyStorePath);
	        keyStore.load(new FileInputStream(f), keyStorePass.toCharArray());
	        
	        // get alias
			String alias = null;			    	       
	    	if(attributes.containsKey(USER_ALIAS)) {
	    		alias = (String) attributes.get(USER_ALIAS);
	    		if(!keyStore.containsAlias(alias))
	    			throw new NoSuccess("The keystore does not contain the '"+alias+"' alias");
	    	}
	    	else {
	    		// only one key must be in the JKS
		    	int numberOfPrivateKey = 0;
		    	Enumeration knownKeyAliases = keyStore.aliases();
		    	while (knownKeyAliases.hasMoreElements()) {
					String next = (String) knownKeyAliases.nextElement();
					if (keyStore.isKeyEntry(next)) {
						numberOfPrivateKey ++;
						alias = next;
					}
		    	}
		    	if(numberOfPrivateKey == 0) {
		    		throw new NoSuccess("The keystore does not contain private key.");
		    	}
		    	else if(numberOfPrivateKey > 1) {
		    		throw new NoSuccess("The keystore contains more than one private key and '"+USER_ALIAS+"' is not defined");
		    	}
	    	}
	    	
	    	// load user key
	        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, userPass.toCharArray());
	    	X509Certificate userCertificate = (X509Certificate) keyStore.getCertificate(alias);
	    	
	    	// load CA keys
	    	Vector loadCerts = new Vector();
			Enumeration knownAliases = keyStore.aliases();
	        while (knownAliases.hasMoreElements()) {
	            String next = (String) knownAliases.nextElement();
	            if (keyStore.isCertificateEntry(next)) {
	                Certificate caCert = keyStore.getCertificate(next);
	                if (caCert instanceof X509Certificate) {
	                	loadCerts.add((X509Certificate)caCert);
	                }
	            }
	        }
	    	X509Certificate[] certificates = new X509Certificate[loadCerts.size()];
	    	for (int i = 0; i < certificates.length; i++) {
	    		certificates[i] = (X509Certificate) loadCerts.get(i);
			}
	        return new JKSSecurityAdaptor(privateKey, userCertificate, certificates );   	
    	}
    	catch (Exception e) {
    		throw new NoSuccess(e);
    	}
    }
}
