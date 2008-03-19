package fr.in2p3.jsaga.adaptor.u6;

import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import pl.edu.icm.unicore.security.dsig.DSigException;

import com.intel.gpe.client.impl.security.DelegationHandler;
import com.intel.gpe.client.impl.security.UGSSecurityHandler;
import com.intel.gpe.client.impl.security.UserTokenHandler;
import com.intel.gpe.client.impl.security.WSSecurityHandler;
import com.intel.gpe.clients.impl.SecurityConfigurationException;
import com.intel.gpe.clients.impl.SecurityProvider;
import com.intel.gpe.security.Credential;
import com.intel.gpe.wsclient.Handler;
import com.intel.gpe.wsclient.transport.http.JSSETransport;

public class U6KeystoreSecurityProvider implements SecurityProvider {
    
	 private Credential cred;
    private List<X509Certificate> trustedCerts;
    
    public U6KeystoreSecurityProvider() {
    }

    public JSSETransport getTransport(URL url) {
        synchronized (this) {
            return new JSSETransport(url, cred, trustedCerts, null, -1, null);
        }
    }
    
    public void init(Vector<X509Certificate> certificates, X509Certificate certificate, PrivateKey privateKey) {
		cred = new Credential(privateKey, new X509Certificate[]{certificate});
		this.trustedCerts = certificates;
    }   
    
    public Handler getDelegationHandler(String receiver) throws SecurityConfigurationException {
        if (isCredNull()) {
            throw new SecurityConfigurationException("User identity is not defined");
        }
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.DATE, 1);
        try {
            return new DelegationHandler(
                    cred.getCertificateChain()[0].getSubjectX500Principal().getName(),
                    cred,
                    receiver,
                    notBefore.getTime(),
                    notAfter.getTime(),
                    800);
        } catch (DSigException e) {
            throw new SecurityConfigurationException(e);
        }
    }
    
    public Handler getWSSecurityHandler() throws SecurityConfigurationException {
        if (isCredNull()) {
            throw new SecurityConfigurationException("User identity is not defined");
        }
        return new WSSecurityHandler(cred);
    }

    
    private boolean isCredNull() {
        return cred == null || cred.getPrivateKey() == null || cred.getCertificateChain() == null;
    }
    
    public Handler getUserTokenHandler() throws SecurityConfigurationException {
        if (isCredNull()) {
            throw new SecurityConfigurationException("User identity is not defined");
        }
        X500Principal dn = cred.getCertificateChain()[0].getSubjectX500Principal();
        try {
            return new UserTokenHandler(dn, cred);
        } catch (DSigException e) {
            throw new SecurityConfigurationException(e);
        }
    }

    @Deprecated
    public Handler getSecurityHandler() {
        return new UGSSecurityHandler(cred);
    }
}
