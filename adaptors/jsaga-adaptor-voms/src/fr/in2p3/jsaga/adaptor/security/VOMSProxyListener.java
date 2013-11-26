package fr.in2p3.jsaga.adaptor.security;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.clients.impl.InitListenerAdapter;
import org.italiangrid.voms.clients.impl.ProxyCreationListener;
import org.italiangrid.voms.request.VOMSACRequest;
import org.italiangrid.voms.request.VOMSErrorMessage;
import org.italiangrid.voms.request.VOMSResponse;
import org.italiangrid.voms.request.VOMSServerInfo;
import org.italiangrid.voms.request.VOMSWarningMessage;
import org.italiangrid.voms.store.LSCInfo;

import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.proxy.ProxyCertificate;

public class VOMSProxyListener implements InitListenerAdapter {

    private GlobusGSSCredentialImpl m_proxy = null;
    
    public void proxyCreated(String proxyPath, ProxyCertificate proxy,
            List<String> warnings) {
        try {
            this.m_proxy = new GlobusGSSCredentialImpl(
                    new X509Credential(proxy.getPrivateKey(), proxy.getCertificateChain()),
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GSSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public GlobusGSSCredentialImpl getProxy() {
        return this.m_proxy;
    }
    
    public void notifyValidationResult(VOMSValidationResult arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyErrorsInVOMSReponse(VOMSACRequest arg0,
            VOMSServerInfo arg1, VOMSErrorMessage[] arg2) {
        // TODO Auto-generated method stub
        
    }

    public void notifyVOMSRequestFailure(VOMSACRequest arg0,
            VOMSServerInfo arg1, Throwable arg2) {
        // TODO Auto-generated method stub
        
    }

    public void notifyVOMSRequestStart(VOMSACRequest arg0, VOMSServerInfo arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyVOMSRequestSuccess(VOMSACRequest arg0, VOMSServerInfo arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyWarningsInVOMSResponse(VOMSACRequest arg0,
            VOMSServerInfo arg1, VOMSWarningMessage[] arg2) {
        // TODO Auto-generated method stub
        
    }

    public void notifyNoValidVOMSESError(List<String> arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyVOMSESInformationLoaded(String arg0, VOMSServerInfo arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyVOMSESlookup(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyCredentialLookup(String... arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyLoadCredentialFailure(Throwable arg0, String... arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyLoadCredentialSuccess(String... arg0) {
        // TODO Auto-generated method stub
        
    }

    public boolean onValidationError(ValidationError arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void notifyCertficateLookupEvent(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyCertificateLoadEvent(X509Certificate arg0, File arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyLSCLoadEvent(LSCInfo arg0, File arg1) {
        // TODO Auto-generated method stub
        
    }

    public void notifyLSCLookupEvent(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyHTTPRequest(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyLegacyRequest(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void notifyReceivedResponse(VOMSResponse arg0) {
        // TODO Auto-generated method stub
        
    }

    public void loadingNotification(String arg0, String arg1, Severity arg2,
            Exception arg3) {
        // TODO Auto-generated method stub
        
    }

}
