package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.*;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.proxy.ext.GlobusProxyCertInfoExtension;
import org.globus.gsi.proxy.ext.ProxyCertInfoExtension;
import org.globus.tools.ProxyInit;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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

    public GlobusProxyFactoryAbstract(String passphrase) {
        m_exception = null;
        m_passphrase = passphrase;
        userKey = null;
    }

    public abstract GSSCredential createProxy() throws GSSException;

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
            certificates = CertUtil.loadCertificates(arg);
        } catch(IOException e) {
            m_exception = new IOException("Failed to load cert: "+arg);
        } catch(GeneralSecurityException e) {
            m_exception = new GeneralSecurityException("Unable to load user certificate: "+e.getMessage());
        }
    }

    public void loadKey(String arg) {
        try {
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
        } catch(IOException e) {
            m_exception = new IOException("Failed to load key: "+arg);
        } catch(GeneralSecurityException e) {
            m_exception = new GeneralSecurityException("Wrong pass phrase");
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
}
