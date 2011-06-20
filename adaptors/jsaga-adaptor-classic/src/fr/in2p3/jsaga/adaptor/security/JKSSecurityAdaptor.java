package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JKSSecurityAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 mars 2008
* ***************************************************/

public class JKSSecurityAdaptor implements SecurityAdaptor {

	//private static final String KEYSTORE = "Keystore";
	//private static final String KEYSTORE_PASS = "KeystorePass";
	private static final String USER_ALIAS = "UserAlias";
	
    public String getType() {
    	return "JKS";
    }

    public Class getSecurityCredentialClass() {
        return JKSSecurityCredential.class;
    }

    public Usage getUsage() {
    	return new UAnd(
    			 new Usage[]{
    					 //new UFile(KEYSTORE), 
    					 //new U(KEYSTORE_PASS),
    					 new UOptional(USER_ALIAS),
    					 new UOptional(Context.USERPASS)});
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
        /*return new Default[]{
        		 new Default(KEYSTORE, new File[]{
                         new File(System.getProperty("user.home")+"/.keystore")}),
        };*/
    	return new Default[]{};
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
    	
    	try {
			KeyStore keyStore = KeyStore.getInstance("jks");
	    	// load private key
    		//String keyStorePass = (String) attributes.get(KEYSTORE_PASS);
			//String keyStorePath = (String) attributes.get(KEYSTORE);
    		String keyStorePath = System.getProperty(EngineProperties.JAVAX_NET_SSL_KEYSTORE);
    		String keyStorePass = System.getProperty(EngineProperties.JAVAX_NET_SSL_KEYSTOREPASSWORD);

    		if (keyStorePath == null)
    			throw new NoSuccessException("The property "+ EngineProperties.JAVAX_NET_SSL_KEYSTORE + " was not set.");
    		
			// load the keystore
	        File f = new File(keyStorePath);
	        keyStore.load(new FileInputStream(f), (keyStorePass != null)?keyStorePass.toCharArray():null);
	        
	        // get alias
			String alias = null;			    	       
	    	if(attributes.containsKey(USER_ALIAS)) {
	    		alias = (String) attributes.get(USER_ALIAS);
	    		if(!keyStore.containsAlias(alias))
	    			throw new NoSuccessException("The keystore does not contain the '"+alias+"' alias");
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
		    		throw new NoSuccessException("The keystore does not contain private key.");
		    	}
		    	else if(numberOfPrivateKey > 1) {
		    		throw new NoSuccessException("The keystore contains more than one private key and '"+USER_ALIAS+"' is not defined");
		    	}
	    	}
	    	
	    	// load user key
	        //PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, userPass.toCharArray());
	    	//X509Certificate userCertificate = (X509Certificate) keyStore.getCertificate(alias);

    		// load CA certs
    		String trustStorePath = System.getProperty(EngineProperties.JAVAX_NET_SSL_TRUSTSTORE);
    		KeyStore trustStore;
    		if (keyStorePath.equals(trustStorePath)) {
    			// private key and CA certs in the same file
    			trustStore = keyStore;
    		} else {
        		String trustStorePass = System.getProperty(EngineProperties.JAVAX_NET_SSL_TRUSTSTOREPASSWORD);
    			trustStore = KeyStore.getInstance("jks");
    			char[] password = (trustStorePass != null)?trustStorePass.toCharArray():null;
    			if (trustStorePath != null) {
    				f = new File(trustStorePath);
    	        	trustStore.load(new FileInputStream(f), password);
    			} else {
    				// first try $JAVA_HOME/lib/security/jssecacerts
	    	        try {
	    	        	f = new File(System.getProperty("java.home")+"/lib/security/jssecacerts");
	    	        	trustStore.load(new FileInputStream(f), password);
	    	        } catch (FileNotFoundException fnfe) {
	    				// then try $JAVA_HOME/lib/security/cacerts
	    	        	f = new File(System.getProperty("java.home")+"/lib/security/cacerts");
	    	        	trustStore.load(new FileInputStream(f), password);
	    	        }
    			}
    		}

	    	// load CA keys
	    	Vector loadCerts = new Vector();
			Enumeration knownAliases = trustStore.aliases();
	        while (knownAliases.hasMoreElements()) {
	            String next = (String) knownAliases.nextElement();
	            if (trustStore.isCertificateEntry(next)) {
	                Certificate caCert = trustStore.getCertificate(next);
	                if (caCert instanceof X509Certificate) {
	                	loadCerts.add((X509Certificate)caCert);
	                }
	            }
	        }
	    	X509Certificate[] certificates = new X509Certificate[loadCerts.size()];
	    	for (int i = 0; i < certificates.length; i++) {
	    		certificates[i] = (X509Certificate) loadCerts.get(i);
			}

			// get optional attributes
			//String userPass = (String) attributes.get(KEYSTORE_PASS);
			String userPass = keyStorePass;
			if (attributes.containsKey(Context.USERPASS)) {
				userPass = (String) attributes.get(Context.USERPASS);
			}
			
	        return new JKSSecurityCredential(keyStore, keyStorePass, alias, userPass, certificates);
    	}
    	catch (Exception e) {
    		throw new NoSuccessException(e);
    	}
    }
}
