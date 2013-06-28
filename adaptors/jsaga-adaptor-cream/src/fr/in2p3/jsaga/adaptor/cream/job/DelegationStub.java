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
import org.glite.ce.security.delegation.DelegationServiceStub.Destroy;
import org.glite.ce.security.delegation.DelegationServiceStub.DestroyResponse;
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
        // create stub
        m_proxyFile = getDlgorFile(host, vo);
		try {
			m_stub = new DelegationServiceStub(new URL("https", host, port, "/ce-cream/services/gridsite-delegation").toString());
		} catch (AxisFault e) {
			throw new NoSuccessException(e);
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
    }

    public void destroy(Destroy destroy2) throws RemoteException, DelegationException_Fault {
    	m_stub.destroy(destroy2);
    }
    
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
            if (e.getMessage()!=null && e.getMessage().contains("not found")) {
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
        if (pkcs10 != null) {
            // set delegation lifetime
            int hours = (int) (globusProxy.getTimeLeft() / 3600) - 1;
            if (hours < 0) {
                throw new AuthenticationFailedException("Proxy is expired or about to expire: "+globusProxy.getIdentity());
            }

            try {
				return signRequest(pkcs10, delegationId, hours);
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
    
    private String signRequest(String certReq, String delegationID, int hours)
            throws IOException, KeyStoreException, CertificateException,
            InvalidKeyException, SignatureException,
            NoSuchAlgorithmException, NoSuchProviderException {
        
        X509Certificate[] parentChain = null;
        PrivateKey pKey = null;
        
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
            
        PEMReader pemReader = new PEMReader(new StringReader(certReq));
        PKCS10CertificationRequest proxytReq = (PKCS10CertificationRequest) pemReader.readObject();
        ProxyRequestOptions csrOpt = new ProxyRequestOptions(parentChain, proxytReq);
        csrOpt.setLifetime(hours*3600);
        
        X509Certificate[] certChain = ProxyGenerator.generate(csrOpt, pKey);
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        for (X509Certificate tmpcert : certChain) {
            CertificateUtils.saveCertificate(outStream, tmpcert, CertificateUtils.Encoding.PEM);
        }
        
        return outStream.toString();

    }	
    
}
