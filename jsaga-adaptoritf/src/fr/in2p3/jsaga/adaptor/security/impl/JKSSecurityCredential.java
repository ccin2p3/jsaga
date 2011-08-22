package fr.in2p3.jsaga.adaptor.security.impl;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JKSSecurityCredential
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   17 mars 2008
* ***************************************************
* Description:                                      */

public class JKSSecurityCredential extends X509SecurityCredential {

	private X509Certificate[] caCertificates;
	private String m_keyStorePath;
	private String m_trustStorePath;
	private String m_trustStorePass;
	
    public JKSSecurityCredential(KeyStore keyStore, String keyStorePass, String userAlias, String userPass, X509Certificate[] caCertificates) throws Exception {
        super(keyStore, keyStorePass, userAlias, userPass);
        this.caCertificates = caCertificates;
    }

    public X509Certificate[] getCaCertificates() {
		return caCertificates;
	}

	public void setKeyStorePath(String m_keyStorePath) {
		this.m_keyStorePath = m_keyStorePath;
	}

	public String getKeyStorePath() {
		return m_keyStorePath;
	}

	public void setTrustStorePath(String m_trustStorePath) {
		this.m_trustStorePath = m_trustStorePath;
	}

	public String getTrustStorePath() {
		return m_trustStorePath;
	}

	public void setTrustStorePass(String m_trustStorePass) {
		this.m_trustStorePass = m_trustStorePass;
	}

	public String getTrustStorePass() {
		return m_trustStorePass;
	}
   
}
