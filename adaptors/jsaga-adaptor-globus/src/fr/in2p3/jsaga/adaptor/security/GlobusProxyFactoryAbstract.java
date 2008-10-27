package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.*;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.proxy.ext.GlobusProxyCertInfoExtension;
import org.globus.gsi.proxy.ext.ProxyCertInfoExtension;
import org.globus.tools.ProxyInit;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusProxyFactoryAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GlobusProxyFactoryAbstract extends ProxyInit {
    protected Exception m_exception;
    private String m_passphrase;
    private PrivateKey userKey;
    private int certificateFormat = 0;

    protected static final int CERTIFICATE_PEM = 0;
    protected static final int CERTIFICATE_PKCS12 = 1;
    protected static final int CERT = 0;
    private static final int KEY = 1;

    public GlobusProxyFactoryAbstract(String passphrase) {
        m_exception = null;
        m_passphrase = passphrase;
        userKey = null;
    }

    public abstract GSSCredential createProxy() throws IncorrectStateException, NoSuccessException;

    public void init(String [] args) {}

    public void verify() throws Exception {
        RSAPublicKey pkey = (RSAPublicKey)getCertificate().getPublicKey();
        RSAPrivateKey prkey = (RSAPrivateKey) userKey;
        if (!pkey.getModulus().equals(prkey.getModulus())) {
            throw new Exception("Certificate and private key specified do not match");
        }
        super.verify();
    }

    public void loadCertificates(String arg) {
        try {
        	if(this.getCertificateFormat() == CERTIFICATE_PEM) {
        		certificates = CertUtil.loadCertificates(arg);
        	}
        	else if(this.getCertificateFormat() == CERTIFICATE_PKCS12) {
        		loadPKCS12Certificate(arg, CERT);
        	}
        } catch(IOException e) {
            m_exception = new IOException("Failed to load certificate file: "+arg);
        } catch(GeneralSecurityException e) {
            m_exception = new GeneralSecurityException("Unable to load user certificate: "+e.getMessage());
        }
    }

    public void loadKey(String arg) {
        try {
        	if(this.getCertificateFormat() == CERTIFICATE_PEM) {
	            OpenSSLKey key = new BouncyCastleOpenSSLKey(arg);
	            if (key.isEncrypted()) {
	                if (m_passphrase != null) {
	                    key.decrypt(m_passphrase);
	                } else {
	                    m_exception = new GeneralSecurityException("You must either provide *UserPass* or decrypt <UserKey>");
	                    return;
	                }
	            }
	            userKey = key.getPrivateKey();
        	}
        	else if(this.getCertificateFormat() == CERTIFICATE_PKCS12) {
        		loadPKCS12Certificate(arg, KEY);
        	}
        } catch(IOException e) {
            m_exception = new IOException("Failed to load key file: "+arg);
        } catch(GeneralSecurityException e) {
            m_exception = new GeneralSecurityException("Wrong pass phrase");
        }
    } 

    public void loadPKCS12Certificate(String arg, int type) throws GeneralSecurityException, IOException   {
    	String s = null, s1 = null;
        try {
        	KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
            keystore.load(new FileInputStream(arg), m_passphrase.toCharArray());
            int i = 0;
            Object obj3 = keystore.aliases();
            do
            {
                if(!((Enumeration) (obj3)).hasMoreElements())
                    break;
                String s4 = (String)((Enumeration) (obj3)).nextElement();
                if(s4.indexOf("CA") != 0) {
                    s = s4;
                    if(++i == 1)
                    {
                        s1 = s;
                        //System.out.println(s + " => found a non CA alias");
                    }
                }
            } while(true);
            //load certificate
            if(type == CERT) {
            	certificates = new X509Certificate[1];
            	certificates[0] = (X509Certificate)keystore.getCertificate(s1);
            }
            // load private key
            else if (type == KEY) {
            	userKey = (PrivateKey)keystore.getKey(s, m_passphrase.toCharArray());
            }
            else {
            	m_exception = new GeneralSecurityException("Unsupported certificate type '"+type+"' : you must must use 0 or 1");
            }
        } catch(IOException e) {
        	throw new IOException("Failed to load PKCS12 cert: "+arg);
        } catch(GeneralSecurityException generalsecurityexception) {
        	throw new GeneralSecurityException("Wrong password or other security error");
        }
    }
    
    public void sign() {
        try {
            BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();
            X509ExtensionSet extSet = null;
            if (proxyCertInfo != null) {
                extSet = new X509ExtensionSet();
                if (CertUtil.isGsi4Proxy(proxyType)) {
                    // RFC compliant OID
                    extSet.add(new ProxyCertInfoExtension(proxyCertInfo));
                } else {
                    // old OID
                    extSet.add(new GlobusProxyCertInfoExtension(proxyCertInfo));
                }
            }
            proxy = factory.createCredential(certificates, userKey, bits, lifetime, proxyType, extSet);
        } catch (GeneralSecurityException e) {
            m_exception = new GeneralSecurityException("Failed to create a proxy: "+e.getMessage());
        }
    }

	public int getCertificateFormat() {
		return certificateFormat;
	}

	public void setCertificateFormat(int certificateFormat) {
		this.certificateFormat = certificateFormat;
	}
}
