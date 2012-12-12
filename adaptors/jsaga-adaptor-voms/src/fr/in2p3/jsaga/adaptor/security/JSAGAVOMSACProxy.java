package fr.in2p3.jsaga.adaptor.security;

import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;

import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;
import org.italiangrid.voms.request.VOMSACRequest;
import org.italiangrid.voms.request.VOMSESLookupStrategy;
import org.italiangrid.voms.request.VOMSRequestListener;
import org.italiangrid.voms.request.impl.BaseVOMSESLookupStrategy;
import org.italiangrid.voms.request.impl.DefaultVOMSACService;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.italiangrid.voms.util.NullListener;

import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.proxy.ProxyCertificate;
import eu.emi.security.authn.x509.proxy.ProxyCertificateOptions;
import eu.emi.security.authn.x509.proxy.ProxyGenerator;
import eu.emi.security.authn.x509.proxy.ProxyPolicy;
import eu.emi.security.authn.x509.proxy.ProxyType;


/**
 * custom based on {@link DefaultVOMSACService} which ease the VOMS Credential creation
 */
public class JSAGAVOMSACProxy extends MyVOMSACService {

    private int proxyLifetime = 86400;
    private int proxyKeyLength = 1024;
    private boolean proxyLimited = false;
    private ProxyType proxyType = ProxyType.LEGACY;
    private ProxyPolicy proxyPolicy = null; //equivalent to: ProxyPolicy.INHERITALL_POLICY_OID

    private static final VOMSESLookupStrategy vomsesLookupStrategy = new BaseVOMSESLookupStrategy(new ArrayList<String>());

    public JSAGAVOMSACProxy(String caDir, VOMSRequestListener vomsRequestListener){
        super(CertificateValidatorBuilder.buildCertificateValidator(caDir), vomsRequestListener, vomsesLookupStrategy, NullListener.INSTANCE, NullListener.INSTANCE);
    }

    public X509Credential getVOMSProxyCertificate(X509Credential credential, VOMSACRequest vomsacRequest) throws CredentialException, VOMSException{
        eu.emi.security.authn.x509.X509Credential emiCred;
        try {
            emiCred = new KeyAndCertCredential(credential.getPrivateKey(),
                    credential.getCertificateChain());
        } catch (KeyStoreException e) {
            throw new CredentialException(e);
        }
        AttributeCertificate attributeCertificate = null;
        if(vomsacRequest != null){
            attributeCertificate = getVOMSAttributeCertificate(emiCred, vomsacRequest);
            if(attributeCertificate == null){
                //TODO: manage if we have only a sub-set of the requested FQANs
                throw new VOMSException("Unable to get a single requested VOMSAttribute");
            }
        }
        ProxyCertificateOptions proxyOptions = new ProxyCertificateOptions(emiCred.getCertificateChain());
        if(attributeCertificate != null){
            proxyOptions.setAttributeCertificates(new AttributeCertificate[] {attributeCertificate});
        }
        proxyOptions.setKeyLength(proxyKeyLength);
        proxyOptions.setLifetime(proxyLifetime);
        proxyOptions.setType(proxyType);
        proxyOptions.setLimited(proxyLimited);
        if(proxyPolicy != null){
            proxyOptions.setPolicy(proxyPolicy);
        }
        ProxyCertificate proxyCert;
        try {
            proxyCert = ProxyGenerator.generate(proxyOptions, emiCred.getKey());
        } catch (InvalidKeyException e) {
            throw new CredentialException(e);
        } catch (CertificateParsingException e) {
            throw new CredentialException(e);
        } catch (SignatureException e) {
            throw new CredentialException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new CredentialException(e);
        }
        return new X509Credential(proxyCert.getPrivateKey(), proxyCert.getCertificateChain());
    }

    public int getProxyKeyLength() {
        return proxyKeyLength;
    }

    public void setProxyKeyLength(int proxyKeyLength) {
        this.proxyKeyLength = proxyKeyLength;
    }

    public int getProxyLifetime() {
        return proxyLifetime;
    }

    public void setProxyLifetime(int proxyLifetime) {
        this.proxyLifetime = proxyLifetime;
    }

    public boolean isProxyLimited() {
        return proxyLimited;
    }

    /**
     * Use it only for legacy proxy type, otherwise use {@link ProxyPolicy}
     * @param proxyLimited
     */
    public void setProxyLimited(boolean proxyLimited) {
        this.proxyLimited = proxyLimited;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public ProxyPolicy getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(ProxyPolicy proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public static class VOMSException extends Throwable {
        private static final long serialVersionUID = 1L;

        public VOMSException() {
            super();
        }

        public VOMSException(String s) {
            super(s);
        }

        public VOMSException(String s, Throwable cause) {
            super(s, cause);
        }
    }
}
