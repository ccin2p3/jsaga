package fr.in2p3.jsaga.adaptor.security;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(VOMSProxyListener.class);
    
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
    
    public void notifyValidationResult(VOMSValidationResult result) {
        logger.info("VOMSValidation results: " + result.isValid());
    }

    public void notifyErrorsInVOMSReponse(VOMSACRequest request, VOMSServerInfo si, VOMSErrorMessage[] errors) {
        logger.error("Errors In VOMS Reponse : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si + "\n\t- errors: " + Arrays.toString(errors));
    }

    public void notifyVOMSRequestFailure(VOMSACRequest request, VOMSServerInfo endpoint, Throwable error) {
        logger.error("Errors In VOMS Reponse : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- endpoint: " + endpoint + "\n\t- errors: " + error);
    }

    public void notifyVOMSRequestStart(VOMSACRequest request, VOMSServerInfo si) {
        logger.debug("VOMS Request Start : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si);
    }

    public void notifyVOMSRequestSuccess(VOMSACRequest request, VOMSServerInfo endpoint) {
        logger.info("VOMS Request Success : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- endpoint: " + endpoint);
    }

    public void notifyWarningsInVOMSResponse(VOMSACRequest request, VOMSServerInfo si, VOMSWarningMessage[] warnings) {
        logger.warn("Warnings In VOMS Response : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si + "\n\t- warnings: " + Arrays.toString(warnings));
        
    }

    public void notifyNoValidVOMSESError(List<String> arg0) {
        logger.error("No valid VOMSES information found");
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

    public void notifyLoadCredentialFailure(Throwable error, String... locations) {
        logger.error("Could not load credential : \n\t -locations: " + Arrays.toString(locations) + "\n\t -error: " + error);
    }

    public void notifyLoadCredentialSuccess(String... locations) {
        logger.info("Loaded credential : \n\t -locations: " + Arrays.toString(locations));
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
