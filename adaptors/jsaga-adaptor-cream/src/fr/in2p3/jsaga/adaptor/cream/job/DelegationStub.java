package fr.in2p3.jsaga.adaptor.cream.job;

import org.apache.axis2.AxisFault;
import org.apache.commons.httpclient.protocol.Protocol;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.glite.ce.creamapi.ws.cream2.Authorization_Fault;
import org.glite.ce.creamapi.ws.cream2.CREAMStub;
import org.glite.ce.security.delegation.DelegationException_Fault;
import org.glite.ce.security.delegation.DelegationServiceStub;
import org.glite.ce.security.delegation.DelegationServiceStub.DelegationException;
import org.glite.ce.security.delegation.DelegationServiceStub.GetProxyReq;
import org.glite.ce.security.delegation.DelegationServiceStub.GetTerminationTime;
import org.glite.ce.security.delegation.DelegationServiceStub.GetTerminationTimeResponse;
import org.glite.ce.security.delegation.DelegationServiceStub.PutProxy;
import org.glite.ce.security.delegation.DelegationServiceStub.RenewProxyReq;
//import org.glite.ce.security.delegation.Delegation;
//import org.glite.ce.security.delegation.DelegationException;
//import org.glite.ce.security.delegation.DelegationServiceLocator;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Properties;

//import javax.xml.rpc.ServiceException;

import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.emi.security.authn.x509.proxy.ProxyGenerator;
import eu.emi.security.authn.x509.proxy.ProxyRequestOptions;
import eu.emi.security.canl.axis2.CANLAXIS2SocketFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DelegationStub
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 dec. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DelegationStub {
    public static final String ANY_VO = null;

    private File m_proxyFile;
    private DelegationServiceStub m_stub;

    public DelegationStub(String host, int port, String vo) throws BadParameterException, NoSuccessException {
        // set endpoint
        URL epr;
        Protocol.registerProtocol("https", new Protocol("https", new CANLAXIS2SocketFactory(), 8443));
        
        Properties sslConfig = new Properties();
        sslConfig.put("truststore", "/home/schwarz/.jsaga/contexts/voms/certificates/");
        sslConfig.put("crlcheckingmode", "ifvalid");
        sslConfig.put("proxy", "/home/schwarz/.jsaga/tmp/voms_cred.txt");
        CANLAXIS2SocketFactory.setCurrentProperties(sslConfig);

        try {
            epr = new URL("https", host, port, "/ce-cream/services/gridsite-delegation");
        } catch (MalformedURLException e) {
            throw new BadParameterException(e.getMessage(), e);
        }

        // create stub
        m_proxyFile = getDlgorFile(host, vo);
//            if (epr.getProtocol().startsWith("https")) {
//                System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
//                System.setProperty("gridProxyFile", m_proxyFile.getAbsolutePath());
//            }
//            DelegationServiceLocator delegationLocator = new DelegationServiceLocator();
//            try {
//				m_stub = delegationLocator.getGridsiteDelegation(epr);
//			} catch (ServiceException e) {
//				throw new NoSuccessException(e);
//			}
		try {
			m_stub = new DelegationServiceStub(new URL("https", host, port, "/ce-cream/services/gridsite-delegation").toString());
		} catch (AxisFault e) {
			throw new NoSuccessException(e);
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
    }

//    public Delegation getStub() {
//        return m_stub;
//    }

    /**
     * Renew delegation, or create a new delegation if it does not exist.
     * @return null if delegation is renewed, or proxy if delegation is created
     */
    public String renewDelegation(String delegationId, GSSCredential cred) throws AuthenticationFailedException {
        // save proxy to file (dlgor does not support in-memory proxy)
        X509Credential globusProxy;
        if (cred instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)cred).getX509Credential();
            try {
                OutputStream out = new FileOutputStream(m_proxyFile);
                globusProxy.save(out);
                out.close();
            } catch (IOException e) {
                throw new AuthenticationFailedException(e);
            } catch (CredentialException e) {
                throw new AuthenticationFailedException(e);
			}
        } else {
            throw new AuthenticationFailedException("Not a globus proxy: "+cred.getClass());
        }

        // renew/create delegated proxy
        String pkcs10 = null;
        try {
        	GetTerminationTime gtt = new GetTerminationTime();
        	gtt.setDelegationID(delegationId);
        	Calendar cal = m_stub.getTerminationTime(gtt).getGetTerminationTimeReturn();
            if (cal.before(Calendar.getInstance())) {
                // renew delegation
            	RenewProxyReq rpq = new RenewProxyReq();
            	rpq.setDelegationID(delegationId);
                pkcs10 = m_stub.renewProxyReq(rpq).getRenewProxyReqReturn();
            }
		} catch (RemoteException e) {
			// TODO: check this
        	// New CreamCE sends a RemoteException when delegationId not found
            if (e.getMessage()!=null && e.getMessage().startsWith("not found")) {
                // create a new delegation
                try {
                   	GetProxyReq gpr = new GetProxyReq();
                	gpr.setDelegationID(delegationId);
 					pkcs10 = m_stub.getProxyReq(gpr).getGetProxyReqReturn();
				} catch (RemoteException e1) {
		            throw new AuthenticationFailedException(e);
				} catch (DelegationException_Fault e1) {
		            throw new AuthenticationFailedException(e);
				}
            } else {
                // rethrow exception
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
		} catch (DelegationException_Fault e) {
			// TODO: check this
            if (e.getMessage()!=null && e.getMessage().startsWith("Failed to find delegation ID")) {
                // create a new delegation
                try {
                   	GetProxyReq gpr = new GetProxyReq();
                	gpr.setDelegationID(delegationId);
 					pkcs10 = m_stub.getProxyReq(gpr).getGetProxyReqReturn();
				} catch (RemoteException e1) {
		            throw new AuthenticationFailedException(e);
				} catch (DelegationException_Fault e1) {
		            throw new AuthenticationFailedException(e);
				}
            } else {
                // rethrow exception
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
		}
/*
    } catch (DelegationException e) {
            if (e.getMsg()!=null && e.getMsg().startsWith("Failed to find delegation ID")) {
                // create a new delegation
                try {
                    pkcs10 = m_stub.getProxyReq(delegationId);
                } catch (DelegationException e2) {
                    throw new AuthenticationFailedException(e2.getMsg(), e2);
                } catch (RemoteException e2) {
                    throw new AuthenticationFailedException(e2);
                }
            } else {
                // rethrow exception
                throw new AuthenticationFailedException(e.getMsg(), e);
            }
        } catch (AuthorizationFault e) {
        	// If operation getTermintationTime is not allowed
//        	String exceptionDescription = getDescription(e);
//        	if (exceptionDescription!= null && exceptionDescription.contains("not authorized for operation")) {
//                try {
//                    pkcs10 = m_stub.getProxyReq(delegationId);
//                } catch (DelegationException e2) {
//                    throw new AuthenticationFailedException(e2.getMsg(), e2);
//                } catch (RemoteException e2) {
//                    throw new AuthenticationFailedException(e2);
//                }
//        	} else {
	        	rethrowAuthorizationException(e);
//        	}
        } catch (RemoteException e) {
        	// New CreamCE sends a RemoteException when delegationId not found
            if (e.getMessage()!=null && (e.getMessage().contains("not found"))) {
                // create a new delegation
                try {
                    pkcs10 = m_stub.getProxyReq(delegationId);
                } catch (DelegationException e2) {
                    throw new AuthenticationFailedException(e2.getMsg(), e2);
                } catch (RemoteException e2) {
                    throw new AuthenticationFailedException(e2);
                }
            } else {
                // rethrow exception
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        }
*/
        if (pkcs10 != null) {
            // set delegation lifetime
            int hours = (int) (globusProxy.getTimeLeft() / 3600) - 1;
            if (hours < 0) {
                throw new AuthenticationFailedException("Proxy is expired or about to expire: "+globusProxy.getIdentity());
            }

            // sign delegated proxy
//            try {
//                GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
//                proxyGenerator.setLifetime(hours);                 
//                byte[] x509Cert = proxyGenerator.x509MakeProxyCert(
//                        pkcs10.getBytes(),
//                        GrDPX509Util.getFilesBytes(m_proxyFile),
//                        "null");
//                String delegProxy = new String(x509Cert);
//                return delegProxy;
//            } catch (IOException e) {
//                throw new AuthenticationFailedException(e);
//            } catch (GeneralSecurityException e) {
//                throw new AuthenticationFailedException(e);
//            }
            try {
				return signRequest(pkcs10, delegationId);
			} catch (InvalidKeyException e) {
				throw new AuthenticationFailedException(e);
			} catch (CertificateException e) {
				throw new AuthenticationFailedException(e);
			} catch (SignatureException e) {
				throw new AuthenticationFailedException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new AuthenticationFailedException(e);
			} catch (NoSuchProviderException e) {
				throw new AuthenticationFailedException(e);
			} catch (IOException e) {
				throw new AuthenticationFailedException(e);
			} catch (KeyStoreException e) {
				throw new AuthenticationFailedException(e);
			}
        } else {
            return null;
        }
    }

    public void putProxy(String delegationId, String delegProxy) throws NoSuccessException {
        	PutProxy pp = new PutProxy();
        	pp.setDelegationID(delegationId);
        	pp.setProxy(delegProxy);
            try {
				m_stub.putProxy(pp);
			} catch (RemoteException e) {
				throw new NoSuccessException(e);
			} catch (DelegationException_Fault e) {
				throw new NoSuccessException(e);
			}
    }

    public static File getDlgorFile(String host, String vo) {
        if (vo != null) {
            return new File(System.getProperty("java.io.tmpdir"), "dlgor_"+host+"_"+vo+"_"+System.getProperty("user.name"));
        } else {
            return new File(System.getProperty("java.io.tmpdir"), "dlgor_"+host+"_"+System.getProperty("user.name"));
        }
    }
    
//    private void rethrowAuthorizationException(AuthorizationFault af) throws AuthenticationFailedException {
//    	try {
//    		throw new AuthenticationFailedException(af.getFaultDetails()[0].getElementsByTagNameNS("http://glite.org/2007/11/ce/cream/types", "Description").item(0).getFirstChild().getNodeValue());
//    	} catch (NullPointerException npe) {
//    		throw new AuthenticationFailedException(af);
//    	} catch (ArrayIndexOutOfBoundsException aioobe) {
//    		throw new AuthenticationFailedException(af);
//    	}
//    }
    
//    private String getDescription(AuthorizationException af) {
//		org.w3c.dom.Element[] details = af.getFaultDetails();
//		if (details != null && details.length>0) {
//			return details[0].getElementsByTagNameNS("http://glite.org/2007/11/ce/cream/types", "Description").item(0).getFirstChild().getNodeValue();
//		} else {
//			return null;
//		}
//    }
    
    private String signRequest(String certReq, String delegationID)
            throws IOException, KeyStoreException, CertificateException,
            InvalidKeyException, SignatureException,
            NoSuchAlgorithmException, NoSuchProviderException {
        
//        String confFileName = System.getProperty("user.home") + "/.glite/dlgor.properties";
//        Properties dlgorOpt = this.loadProperties(confFileName);
        
        X509Certificate[] parentChain = null;
        PrivateKey pKey = null;
        
//        String proxyFilename = dlgorOpt.getProperty("issuerProxyFile", "");
//        String certFilename = dlgorOpt.getProperty("issuerCertFile", "");
//        String keyFilename = dlgorOpt.getProperty("issuerKeyFile", "");
//        String passwd = dlgorOpt.getProperty("issuerPass", "");
        
//        if (proxyFilename.length() == 0) {
//            
//            if (certFilename.length() == 0) {
//                throw new AxisFault("Missing user credentials: issuerCertFile not found in " + confFileName);
//            }
//            
//            if (keyFilename.length() == 0) {
//                throw new AxisFault("Missing user credentials: issuerKeyFile not found in " + confFileName);
//            }
//            
//            char[] tmppwd = null;
//            if (passwd.length() != 0) {
//                tmppwd = passwd.toCharArray();
//            }
//            
//            FileInputStream inStream = null;
//            try {
//                inStream = new FileInputStream(keyFilename);
//                pKey = CertificateUtils.loadPrivateKey(inStream, CertificateUtils.Encoding.PEM, tmppwd);
//            } finally {
//                if (inStream != null) {
//                    inStream.close();
//                }
//            }
//                        
//            inStream = null;
//            try {
//                inStream = new FileInputStream(certFilename);
//                parentChain = CertificateUtils.loadCertificateChain(inStream, CertificateUtils.Encoding.PEM);
//            } finally {
//                if (inStream != null) {
//                    inStream.close();
//                }
//            }
//            
//        }else{
            
            FileInputStream inStream = null;
            try {
                
                inStream = new FileInputStream(m_proxyFile);
                PEMCredential credentials = new PEMCredential(inStream, (char[]) null);
                pKey = credentials.getKey();
                parentChain = credentials.getCertificateChain();
                
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
            
//        }
            
        
        PEMReader pemReader = new PEMReader(new StringReader(certReq));
        PKCS10CertificationRequest proxytReq = (PKCS10CertificationRequest) pemReader.readObject();
        ProxyRequestOptions csrOpt = new ProxyRequestOptions(parentChain, proxytReq);
        
        X509Certificate[] certChain = ProxyGenerator.generate(csrOpt, pKey);
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        for (X509Certificate tmpcert : certChain) {
            CertificateUtils.saveCertificate(outStream, tmpcert, CertificateUtils.Encoding.PEM);
        }
        
        return outStream.toString();

    }	
    
//    private Properties loadProperties(String filename) throws IOException {
//        Properties dlgorOpt = new Properties();
//        
//        FileInputStream inStream = null;
//        try {
//            inStream = new FileInputStream(filename);
//            dlgorOpt.load(inStream);
//        } finally {
//            if (inStream != null) {
//                    inStream.close();
//            }
//        }
//        
//        return dlgorOpt;
//
//    }
    
}
