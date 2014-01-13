package fr.in2p3.jsaga.adaptor.security;

import java.io.FileInputStream;

import org.bouncycastle.openssl.PasswordFinder;
import org.italiangrid.voms.clients.util.VOMSProxyPathBuilder;
import org.italiangrid.voms.credential.LoadCredentialsEventListener;
import org.italiangrid.voms.credential.impl.AbstractLoadCredentialsStrategy;
import org.italiangrid.voms.util.FilePermissionHelper;

import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.PEMCredential;

/*
 * FIXME: remove this class when voms-api-java works on Windows
 */
public class JSAGALoadProxyCredential extends AbstractLoadCredentialsStrategy {

    final String proxyFile;
    LoadCredentialsEventListener listener;
    
    public JSAGALoadProxyCredential(LoadCredentialsEventListener listener, String proxyFile) {
        super(listener);
        this.proxyFile = proxyFile;
        this.listener = listener;
    }
    
    public JSAGALoadProxyCredential(LoadCredentialsEventListener listener) {
        this(listener, null);
        this.listener = listener;
    }
    
    
    public X509Credential loadCredentials(PasswordFinder passwordFinder) {
        
        if (proxyFile == null){
            String envProxyPath = System.getenv(X509_USER_PROXY);
            if (envProxyPath != null)
                return loadProxyCredential(envProxyPath);
            
            return loadProxyCredential(VOMSProxyPathBuilder.buildProxyPath());
        }
        
        return loadProxyCredential(proxyFile);
    }

    /**
     * Loads an X.509 proxy credential and notifies the registered {@link LoadCredentialsEventListener} of
     * the load operation outcome.
     * 
     * @param proxyPath the path to the proxy credential
     * @return the loaded {@link X509Credential}, or <code>null</code> if the credential couldn't be loaded 
     */
    protected X509Credential loadProxyCredential(String proxyPath){
        PEMCredential cred = null;
        
        listener.notifyCredentialLookup(proxyPath);
        
        try {
            if (!System.getProperty("os.name").startsWith("Windows")) {
                FilePermissionHelper.checkProxyPermissions(proxyPath);
            }
            cred = new PEMCredential(new FileInputStream(proxyPath), (char[])null);
            listener.notifyLoadCredentialSuccess(proxyPath);
        
        } catch (Throwable t) {
            
            listener.notifyLoadCredentialFailure(t, proxyPath);
        }
        
        return cred;    
    }
}
