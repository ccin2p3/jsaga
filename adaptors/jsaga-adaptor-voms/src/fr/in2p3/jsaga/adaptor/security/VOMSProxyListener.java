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
import org.italiangrid.voms.request.VOMSACRequest;
import org.italiangrid.voms.request.VOMSErrorMessage;
import org.italiangrid.voms.request.VOMSResponse;
import org.italiangrid.voms.request.VOMSServerInfo;
import org.italiangrid.voms.request.VOMSWarningMessage;
import org.italiangrid.voms.store.LSCInfo;
import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.proxy.ProxyCertificate;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSProxyListener
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description: */

public class VOMSProxyListener implements InitListenerAdapter {

    private GlobusGSSCredentialImpl m_proxy = null;
    private static final Logger logger = Logger.getLogger(VOMSProxyListener.class);
    private String m_exceptionMessage = null;
    
    public String getError() {
        return m_exceptionMessage;
    }
    
    public void proxyCreated(String proxyPath, ProxyCertificate proxy, List<String> warnings) {
        try {
            this.m_proxy = new GlobusGSSCredentialImpl(
                    new X509Credential(proxy.getPrivateKey(), proxy.getCertificateChain()),
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (IllegalStateException e) {
            logger.error("Could not build GSSCredential", e);
        } catch (GSSException e) {
            logger.error("Could not build GSSCredential", e);
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
        this.m_exceptionMessage = errors[0].toString();
    }

    public void notifyVOMSRequestFailure(VOMSACRequest request, VOMSServerInfo endpoint, Throwable error) {
        logger.error("VOMS Request failure : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- endpoint: " + endpoint + "\n\t- errors: " + error);
        // in case of server unreachable: keep exception for future usage
        if (error instanceof org.italiangrid.voms.request.VOMSProtocolError) {
            m_exceptionMessage = error.getMessage();
        }
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

    public void notifyNoValidVOMSESError(List<String> paths) {
        logger.error("No valid VOMSES information found : \n\t" + Arrays.toString(paths.toArray()));
    }

    public void notifyVOMSESInformationLoaded(String path, VOMSServerInfo endpoint) {
        logger.debug("VOMS information loaded : \n\t- path:" + path + "\n\t- endpoint: " + endpoint);
    }

    public void notifyVOMSESlookup(String path) {
        logger.debug("VOMS information loading : \n\t- path:" + path);
    }

    public void notifyCredentialLookup(String... locations) {
        logger.debug("Looking for credential in : " + Arrays.toString(locations));
    }

    public void notifyLoadCredentialFailure(Throwable error, String... locations) {
        logger.error("Could not load credential : \n\t -locations: " + Arrays.toString(locations) + "\n\t -error: " + error);
        if (error instanceof java.io.IOException) {
            this.m_exceptionMessage = error.getMessage();
        }
    }

    public void notifyLoadCredentialSuccess(String... locations) {
        logger.info("Loaded credential : \n\t -locations: " + Arrays.toString(locations));
    }

    public boolean onValidationError(ValidationError error) {
        logger.error("Validation error : " + error.getMessage());
        return false;
    }

    public void notifyCertficateLookupEvent(String dir) {
        logger.debug("Looking certificates in : " + dir);
    }

    public void notifyCertificateLoadEvent(X509Certificate cert, File file) {
        logger.debug("AA Certificate for " + cert.getSubjectDN() + " has been loaded from " + file.getAbsolutePath());
    }

    public void notifyLSCLoadEvent(LSCInfo info, File file) {
        logger.debug("LSC info for " + info.toString() + " has been loaded from " + file.getAbsolutePath());
    }

    public void notifyLSCLookupEvent(String dir) {
        logger.debug("Looking LSCInfo in : " + dir);
    }

    public void notifyHTTPRequest(String url) {
        logger.debug("HTTP request is : " + url);
    }

    public void notifyLegacyRequest(String req) {
        logger.debug("Legacy request is : " + req);
    }

    public void notifyReceivedResponse(VOMSResponse response) {
        logger.debug("Received VOMS response : " + response.getXMLAsString());
    }

    public void loadingNotification(String location, String type, Severity severity, Exception error) {
        logger.debug("Updated " + type + " from " + location);
        if (error != null) {
            logger.warn("[" + severity.name() + "] " + error.getMessage());
        }
    }

}
