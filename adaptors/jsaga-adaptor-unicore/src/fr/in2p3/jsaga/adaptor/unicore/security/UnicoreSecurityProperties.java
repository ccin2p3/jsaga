package fr.in2p3.jsaga.adaptor.unicore.security;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import de.fzj.unicore.wsrflite.security.ISecurityProperties;
import de.fzj.unicore.wsrflite.security.UASSecurityProperties;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

public class UnicoreSecurityProperties extends UASSecurityProperties {

	public UnicoreSecurityProperties(Properties parent)
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		super(parent);
	}

	public UnicoreSecurityProperties(JKSSecurityCredential cred) throws UnrecoverableKeyException, KeyStoreException,
	NoSuchAlgorithmException, CertificateException, IOException {
//    	p.put(ISecurityProperties.WSRF_SSL_KEYSTORE, m_credential.getKeyStorePath());
//    	p.put(ISecurityProperties.WSRF_SSL, "true");
//    	p.put(ISecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
//		
//        //keystore and truststore locations
//    	p.put(ISecurityProperties.WSRF_SSL_KEYSTORE, m_credential.getKeyStorePath());
//    	p.put(ISecurityProperties.WSRF_SSL_KEYPASS, m_credential.getKeyStorePass());
//    	p.put(ISecurityProperties.WSRF_SSL_KEYALIAS, m_credential.getKeyStoreAlias());
//    	p.put(ISecurityProperties.WSRF_SSL_TRUSTSTORE, m_credential.getTrustStorePath());
//    	if (m_credential.getTrustStorePass() != null) {
//    		p.put(ISecurityProperties.WSRF_SSL_TRUSTPASS, m_credential.getTrustStorePass());
//    	}
    	
		super(new Properties());

        setProperty(ISecurityProperties.WSRF_SSL, "true");
        setProperty(ISecurityProperties.WSRF_SSL_CLIENTAUTH, "true");

        //keystore and truststore locations
        setProperty(ISecurityProperties.WSRF_SSL_KEYSTORE, cred.getKeyStorePath());
        setProperty(ISecurityProperties.WSRF_SSL_KEYPASS, cred.getKeyStorePass());
        setProperty(ISecurityProperties.WSRF_SSL_KEYALIAS, cred.getKeyStoreAlias());
        setProperty(ISecurityProperties.WSRF_SSL_TRUSTSTORE, cred.getTrustStorePath());
        if (cred.getTrustStorePass() != null) {
                setProperty(ISecurityProperties.WSRF_SSL_TRUSTPASS, cred.getTrustStorePass());
        }
		
	}
}
