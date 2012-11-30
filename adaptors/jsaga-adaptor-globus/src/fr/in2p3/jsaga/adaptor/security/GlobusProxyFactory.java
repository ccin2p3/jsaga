package fr.in2p3.jsaga.adaptor.security;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Map;

import org.bouncycastle.openssl.PasswordFinder;
import org.globus.common.Version;
import org.globus.gsi.CredentialException;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GSIConstants.CertificateType;
import org.globus.gsi.X509Credential;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.base.usage.UDuration;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusProxyFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusProxyFactory {
    public static final int OID_OLD = 2;
    public static final int OID_GLOBUS = 3;    // default
    public static final int OID_RFC820 = 4;
    
    private static final int PROXY_BITS = 1024;
    private static final int DEFAULT_PROXY_LIFETIME = 3600 * 12;
    
    protected static final int CERTIFICATE_PEM = 0;
    protected static final int CERTIFICATE_PKCS12 = 1;
    
    private X509Credential m_userCredential = null;
    private String m_proxyFile = "";
    private int m_proxyLifetime = 0;
    private CertificateType m_proxyType = null;
    private String m_cadir = null;

    public GlobusProxyFactory(Map attributes, int oid, int certificateFormat) throws BadParameterException, ParseException {
        // required attributes
    	String passphrase = (String) attributes.get(Context.USERPASS);        
        m_cadir = (String) attributes.get(Context.CERTREPOSITORY);
        m_proxyFile = (String) attributes.get(Context.USERPROXY);
        
        if ("".equals(passphrase)) {
            passphrase = null;
        }
        
        final char[] pwd;
        if(passphrase != null){
        	pwd = passphrase.toCharArray();
        }else{
        	pwd = null;
        }
        switch(certificateFormat) {
	        case CERTIFICATE_PEM:
	            String userCert = (String) attributes.get(Context.USERCERT);
	            String userKey = (String) attributes.get(Context.USERKEY);
				try {
					X509Certificate[] x509Certificates = CertificateLoadUtil.loadCertificates(userCert);
					PrivateKey privateKey = CertificateLoadUtil.loadPrivateKey(userKey, new PasswordFinder() {
						public char[] getPassword() {
							return pwd;
						}
					});
					m_userCredential =  new X509Credential(privateKey, x509Certificates);
				} catch (Exception e) {
					throw new BadParameterException("Unable to load the provided pems files (cert: '" + userCert + "', key: '" + userKey, e);
				}
	            break;
	        case CERTIFICATE_PKCS12:
	            String pkcs12 = (String) attributes.get(GlobusContext.USERCERTKEY);
				try {
					m_userCredential =  CertificateLoadUtil.loadKeystore(pkcs12, passphrase != null ? passphrase.toCharArray() : null, null, null, "PKCS12");
				} catch (Exception e) {
					throw new BadParameterException("Unable to load the provided pkcs12 file (" + pkcs12 + ")");
				}
	            break;
            default:
                throw new BadParameterException("Invalid case, either PEM or PKCS12 certificates is supported");
        }
        
        // optional attributes
        if (attributes.containsKey(Context.LIFETIME)) {
        	m_proxyLifetime = UDuration.toInt(attributes.get(Context.LIFETIME));
        }else{
        	m_proxyLifetime = DEFAULT_PROXY_LIFETIME;
        }
        boolean limited = false;
        if (attributes.containsKey(GlobusContext.DELEGATION)) {
            limited = ((String)attributes.get(GlobusContext.DELEGATION)).equalsIgnoreCase("limited");
        }
        switch(oid) {
            case OID_OLD:
            	m_proxyType = (limited) ?
                        GSIConstants.CertificateType.GSI_2_LIMITED_PROXY :
                        GSIConstants.CertificateType.GSI_2_PROXY;
                break;
            case OID_GLOBUS:
            	m_proxyType = (limited) ?
                        GSIConstants.CertificateType.GSI_3_LIMITED_PROXY :
                        GSIConstants.CertificateType.GSI_3_IMPERSONATION_PROXY;
                break;
            case OID_RFC820:
            	m_proxyType = (limited) ?
                        GSIConstants.CertificateType.GSI_4_LIMITED_PROXY :
                        GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY;
                break;
        }
    }

    public GSSCredential createProxy() throws IncorrectStateException, NoSuccessException {
    	BouncyCastleCertProcessingFactory bouncyCastleCertProcessingFactory = BouncyCastleCertProcessingFactory.getDefault();    	
    	X509Credential proxy;
		try {
			proxy = bouncyCastleCertProcessingFactory.createCredential(m_userCredential.getCertificateChain(), m_userCredential.getPrivateKey(), PROXY_BITS, m_proxyLifetime, m_proxyType);
		} catch (Exception e) {
			throw new NoSuccessException("Unable to generate the user proxy", e);
		}
        try {
			proxy.verify(m_cadir);
		} catch (CredentialException e) {
			if(proxy.getTimeLeft() < 0){
				throw new IncorrectStateException("Your certificate is expired", e);
			}else{
				throw new NoSuccessException("Proxy verification failed", e);
			}
		}
        try {
        	GlobusGSSCredentialImpl globusGSSCredentialImpl = new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_ONLY);
        	proxy.writeToFile(new File(m_proxyFile));
        	Util.setFilePermissions(m_proxyFile, 600);
        	return globusGSSCredentialImpl;
        } catch (GSSException e) {
            throw new NoSuccessException("Proxy convertion failed", e);
        } catch (CredentialException e) {
        	throw new NoSuccessException("Unable to save the generated proxy in '" +m_proxyFile + "'", e);
		} catch (IOException e) {
			throw new NoSuccessException("Unable to save the generated proxy in '" +m_proxyFile + "'", e);
		}
    }
    
    public String getVersion() {
        return Version.getVersion();
    }
}
