package fr.in2p3.jsaga.adaptor.cream.job;

import org.glite.ce.creamapi.ws.cream2.types.AuthorizationFault;
import org.glite.security.delegation.*;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

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
    private Delegation m_stub;

    public DelegationStub(String host, int port, String vo) throws BadParameterException, NoSuccessException {
        // set endpoint
        URL epr;
        try {
            epr = new URL("https", host, port, "/ce-cream/services/gridsite-delegation");
        } catch (MalformedURLException e) {
            throw new BadParameterException(e.getMessage(), e);
        }

        // create stub
        m_proxyFile = getDlgorFile(host, vo);
        try {
            if (epr.getProtocol().startsWith("https")) {
                System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
                System.setProperty("gridProxyFile", m_proxyFile.getAbsolutePath());
            }
            DelegationServiceLocator delegationLocator = new DelegationServiceLocator();
            m_stub = delegationLocator.getGridsiteDelegation(epr);
        } catch (ServiceException e) {
            throw new NoSuccessException(e.getMessage(), e);
        }
    }

    public Delegation getStub() {
        return m_stub;
    }

    /**
     * Renew delegation, or create a new delegation if it does not exist.
     * @return null if delegation is renewed, or proxy if delegation is created
     */
    public String renewDelegation(String delegationId, GSSCredential cred) throws AuthenticationFailedException {
        // save proxy to file (dlgor does not support in-memory proxy)
        GlobusCredential globusProxy;
        if (cred instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)cred).getGlobusCredential();
            try {
                OutputStream out = new FileOutputStream(m_proxyFile);
                globusProxy.save(out);
                out.close();
            } catch (IOException e) {
                throw new AuthenticationFailedException(e);
            }
        } else {
            throw new AuthenticationFailedException("Not a globus proxy: "+cred.getClass());
        }

        // renew/create delegated proxy
        String pkcs10 = null;
        try {
            Calendar cal = m_stub.getTerminationTime(delegationId);
            if (cal.before(Calendar.getInstance())) {
                // renew delegation
                pkcs10 = m_stub.renewProxyReq(delegationId);
            }
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
        	rethrowAuthorizationException(e);
        } catch (RemoteException e) {
            throw new AuthenticationFailedException(e);
        }

        if (pkcs10 != null) {
            // set delegation lifetime
            int hours = (int) (globusProxy.getTimeLeft() / 3600) - 1;
            if (hours < 0) {
                throw new AuthenticationFailedException("Proxy is expired or about to expire: "+globusProxy.getIdentity());
            }

            // sign delegated proxy
            try {
                GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
                proxyGenerator.setLifetime(hours);                 
                byte[] x509Cert = proxyGenerator.x509MakeProxyCert(
                        pkcs10.getBytes(),
                        GrDPX509Util.getFilesBytes(m_proxyFile),
                        "null");
                String delegProxy = new String(x509Cert);
                return delegProxy;
            } catch (IOException e) {
                throw new AuthenticationFailedException(e);
            } catch (GeneralSecurityException e) {
                throw new AuthenticationFailedException(e);
            }
        } else {
            return null;
        }
    }

    public void putProxy(String delegationId, String delegProxy) throws NoSuccessException {
        try {
            m_stub.putProxy(delegationId, delegProxy);
        } catch (DelegationException e) {
            throw new NoSuccessException(e.getMsg(), e);
        } catch (RemoteException e) {
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
    
    private void rethrowAuthorizationException(AuthorizationFault af) throws AuthenticationFailedException {
    	try {
    		throw new AuthenticationFailedException(af.getFaultDetails()[0].getElementsByTagNameNS("http://glite.org/2007/11/ce/cream/types", "Description").item(0).getFirstChild().getNodeValue());
    	} catch (NullPointerException npe) {
    		throw new AuthenticationFailedException(af);
    	} catch (ArrayIndexOutOfBoundsException aioobe) {
    		throw new AuthenticationFailedException(af);
    	}
    }
}
