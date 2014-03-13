package fr.in2p3.jsaga.adaptor.security;

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

    protected static final String KEYSTORE = "Keystore";
    protected static final String KEYSTORE_PASS = "KeystorePass";
    protected static final String TRUSTSTORE = "Truststore";
    protected static final String TRUSTSTORE_PASS = "TruststorePass";
    protected static final String USER_ALIAS = "UserAlias";
    private static final String JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    private static final String JAVAX_NET_SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    private static final String JAVAX_NET_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
    private static final String JAVAX_NET_SSL_TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
    
    public String getType() {
        return "JKS";
    }

    public Class getSecurityCredentialClass() {
        return JKSSecurityCredential.class;
    }

    public Usage getUsage() {
        return new UAnd.Builder()
                         .and(new UOptional(KEYSTORE))
                         .and(new UOptional(KEYSTORE_PASS))
                         .and(new UOptional(TRUSTSTORE))
                         .and(new UOptional(TRUSTSTORE_PASS))
                         .and(new UOptional(USER_ALIAS))
                         .and(new UOptional(Context.USERPASS))
                         .build();
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
        return new Default[]{};
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");

            // system properties as default values
            String keyStorePath = System.getProperty(JAVAX_NET_SSL_KEYSTORE);
            String keyStorePass = System.getProperty(JAVAX_NET_SSL_KEYSTOREPASSWORD);
            // override by the adaptor config
            if (attributes.containsKey(KEYSTORE)) 
                keyStorePath = (String) attributes.get(KEYSTORE);
            if (attributes.containsKey(KEYSTORE_PASS)) 
                keyStorePass = (String) attributes.get(KEYSTORE_PASS);

            if (keyStorePath == null)
                throw new NoSuccessException("The property "+ JAVAX_NET_SSL_KEYSTORE + " was not set.");
            
            // get optional attributes
            //String userPass = (String) attributes.get(KEYSTORE_PASS);
            String userPass = keyStorePass;
            if (attributes.containsKey(Context.USERPASS)) {
                userPass = (String) attributes.get(Context.USERPASS);
            }
            
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
            
            // load CA certs
            String trustStorePath = System.getProperty(JAVAX_NET_SSL_TRUSTSTORE);
            if (attributes.containsKey(TRUSTSTORE)) 
                trustStorePath = (String) attributes.get(TRUSTSTORE);

            KeyStore trustStore;
            String trustStorePass;
            if (keyStorePath.equals(trustStorePath)) {
                // private key and CA certs in the same file
                trustStore = keyStore;
                trustStorePass = keyStorePass;
            } else {
                trustStorePass = System.getProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD);
                if (attributes.containsKey(TRUSTSTORE_PASS)) 
                    trustStorePass = (String) attributes.get(TRUSTSTORE_PASS);
                trustStore = KeyStore.getInstance("jks");
                char[] password = (trustStorePass != null)?trustStorePass.toCharArray():null;
                if (trustStorePath != null) {
                    f = new File(trustStorePath);
                    trustStore.load(new FileInputStream(f), password);
                } else {
                    // first try $JAVA_HOME/lib/security/jssecacerts
                    try {
                        trustStorePath = System.getProperty("java.home")+"/lib/security/jssecacerts";
                        f = new File(trustStorePath);
                        trustStore.load(new FileInputStream(f), password);
                    } catch (FileNotFoundException fnfe) {
                        // then try $JAVA_HOME/lib/security/cacerts
                        trustStorePath = System.getProperty("java.home")+"/lib/security/cacerts";
                        f = new File(trustStorePath);
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
            JKSSecurityCredential jks_sc = new JKSSecurityCredential(keyStore, keyStorePass, alias, userPass, certificates);
            jks_sc.setKeyStorePath(keyStorePath);
            jks_sc.setTrustStorePath(trustStorePath);
            jks_sc.setTrustStorePass(trustStorePass);
            return jks_sc;
        }
        catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
