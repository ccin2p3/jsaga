package fr.in2p3.jsaga.adaptor.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.bouncycastle.openssl.PasswordFinder;
import org.italiangrid.voms.clients.util.VOMSProxyPathBuilder;
import org.italiangrid.voms.credential.LoadCredentialsEventListener;
import org.italiangrid.voms.credential.impl.AbstractLoadCredentialsStrategy;
import org.italiangrid.voms.util.FilePermissionHelper;

import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.KeystoreCredential;
import eu.emi.security.authn.x509.impl.PEMCredential;

/*
 * FIXME: remove this class when voms-api-java works on Windows
 */
public class JSAGALoadUserCredential extends AbstractLoadCredentialsStrategy {

    String certFile;
    String keyFile;
    
    String pkcs12File;
    LoadCredentialsEventListener listener;
    
    public JSAGALoadUserCredential(LoadCredentialsEventListener listener, String certFile, String keyFile){
        super(listener);
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.listener = listener;
    }
    
    public JSAGALoadUserCredential(LoadCredentialsEventListener listener, String pkcs12File){
        super(listener);
        this.pkcs12File = pkcs12File;
        this.listener = listener;
    }
    
    public X509Credential loadCredentials(PasswordFinder passwordFinder) {
        
        if (pkcs12File != null)
            return loadPKCS12Credential(pkcs12File, passwordFinder);
        
        if (certFile != null && keyFile != null)
            return loadPEMCredential(keyFile, certFile, passwordFinder);
        
        return null;
    }

    /**
     * Loads a  PEM X.509 credential and notifies the registered {@link LoadCredentialsEventListener} of
     * the load operation outcome.
     * 
     * @param privateKeyPath the path to the private key
     * @param certificatePath the path to the certificate
     * @param pf a {@link PasswordFinder} used to resolve the private key password when needed
     * @return the loaded {@link X509Credential}, or <code>null</code> if the credential couldn't be loaded 
     */
    protected X509Credential loadPEMCredential(String privateKeyPath, String certificatePath, PasswordFinder pf){
        
        PEMCredential cred = null;
        
        listener.notifyCredentialLookup(privateKeyPath, certificatePath);
        
        try {
                        
            if (!System.getProperty("os.name").startsWith("Windows")) {
                FilePermissionHelper.checkPrivateKeyPermissions(privateKeyPath);
            }
            
            cred =  new PEMCredential(new FileInputStream(privateKeyPath),
                        new FileInputStream(certificatePath),
                        pf);
            
            listener.notifyLoadCredentialSuccess(privateKeyPath, certificatePath);
            
        
        } catch (Throwable t) {
            
            listener.notifyLoadCredentialFailure(t, privateKeyPath, certificatePath);
        }
    
        return cred;
        
    }

    /**
     * Loads a PCKS12 X.509 credential and notifies the registered {@link LoadCredentialsEventListener} of
     * the load operation outcome.
     * 
     * @param pkcs12FilePath the path to the pkcs12 credential
     * @param pf a {@link PasswordFinder} used to resolve the private key password 
     * @return the loaded {@link X509Credential}, or <code>null</code> if the credential couldn't be loaded 
     */
    protected X509Credential loadPKCS12Credential(String pkcs12FilePath, PasswordFinder pf){
        KeystoreCredential cred = null;
        
        listener.notifyCredentialLookup(pkcs12FilePath);
        
        if (fileExistsAndIsReadable(pkcs12FilePath)){
            
            
            char[] keyPassword = pf.getPassword();
            try {
            
                if (!System.getProperty("os.name").startsWith("Windows")) {
                    FilePermissionHelper.checkPKCS12Permissions(pkcs12FilePath);
                }
                
                cred = new KeystoreCredential(pkcs12FilePath, keyPassword, keyPassword, null, "PKCS12");
                listener.notifyLoadCredentialSuccess(pkcs12FilePath);
            
            } catch (Throwable t) {
                
                listener.notifyLoadCredentialFailure(t, pkcs12FilePath);
            }
        
        }else
            listener.notifyLoadCredentialFailure(new FileNotFoundException(pkcs12FilePath+" (cannot read file)"), pkcs12FilePath);
            
        return cred;
    }

}
