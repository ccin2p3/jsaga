package fr.in2p3.jsaga.adaptor.security.impl;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   X509SecurityCredential
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   17 mars 2008
* ***************************************************
* Description:                                      */

public class X509SecurityCredential implements SecurityCredential {

    private KeyManager[] keyManager;
    private PrivateKey privateKey;
    private X509Certificate certificate;
	private static final DateFormat DF = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
	private String m_keyStorePass;
	private String m_KeyStoreAlias;
	
    public X509SecurityCredential(KeyStore keyStore, String keyStorePass, String userAlias, String userPass) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
        keyManager = keyManagerFactory.getKeyManagers();
        privateKey = (PrivateKey) keyStore.getKey(userAlias, userPass.toCharArray());
        certificate = (X509Certificate) keyStore.getCertificate(userAlias);
		this.m_KeyStoreAlias = userAlias;
		this.m_keyStorePass = keyStorePass;
    }

    public KeyManager[] getKeyManager() {
        return keyManager;
    }
    public PrivateKey getPrivateKey() {
		return privateKey;
	}
    public X509Certificate getCertificate() {
        return certificate;
    }

	public void close() throws Exception {
	}
	
	public void dump(PrintStream out) throws Exception {		
		out.println("  subject  \t\t: "+getUserID());
        out.println("  issuer   \t\t: "+ certificate.getIssuerDN().getName());
        out.println("  not valid before \t: "+ DF.format(certificate.getNotBefore()));
        out.println("  not valid after \t: "+ DF.format(certificate.getNotAfter()));
        
	}
	public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
		if (Context.LIFETIME.equals(key)) {
			long remainingTimeInMs = certificate.getNotAfter().getTime() - new Date().getTime();
			if(String.valueOf(remainingTimeInMs).length() > 3)
				return String.valueOf(remainingTimeInMs).substring(0, String.valueOf(remainingTimeInMs).length()-3);
			else
				return "0";
        } else if(Context.USERID.equals(key)) {
        	try {
				return getUserID();
			} catch (Exception e) {
				throw new NoSuccessException(e);
			}
		} else if(Context.USERKEY.equals(key)) {
        	try {
				return privateKey.toString();
			} catch (Exception e) {
				throw new NoSuccessException(e);
			}
		} else if(Context.USERCERT.equals(key)) {
        	try {
				return certificate.toString();
			} catch (Exception e) {
				throw new NoSuccessException(e);
			}
		} else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
	}
	
	public String getUserID() {
		return certificate.getSubjectDN().getName();
	}

	public String getKeyStorePass() {
		return m_keyStorePass;
	}

	public String getKeyStoreAlias() {
		return m_KeyStoreAlias;
	}
}
