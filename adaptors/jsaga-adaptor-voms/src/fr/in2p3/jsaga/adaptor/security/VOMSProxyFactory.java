package fr.in2p3.jsaga.adaptor.security;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.globus.gsi.CredentialException;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSValidators;
import org.italiangrid.voms.request.VOMSACRequest;
import org.italiangrid.voms.request.VOMSErrorMessage;
import org.italiangrid.voms.request.VOMSRequestListener;
import org.italiangrid.voms.request.VOMSServerInfo;
import org.italiangrid.voms.request.VOMSWarningMessage;
import org.italiangrid.voms.request.impl.DefaultVOMSACRequest;
import org.italiangrid.voms.request.impl.DefaultVOMSServerInfo;
import org.italiangrid.voms.store.impl.DefaultVOMSTrustStore;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import eu.emi.security.authn.x509.impl.KeystoreCredential;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.emi.security.authn.x509.proxy.ProxyPolicy;
import eu.emi.security.authn.x509.proxy.ProxyType;
import fr.in2p3.jsaga.adaptor.base.usage.UDuration;
import fr.in2p3.jsaga.adaptor.security.JSAGAVOMSACProxy.VOMSException;

/*
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) *** *** http://cc.in2p3.fr/
 * *** *************************************************** File:
 * VOMSProxyFactory Author: Sylvain Reynaud (sreynaud@in2p3.fr) Date: 21 sept.
 * 2007 *************************************************** Description:
 */
/**
 *
 */
public class VOMSProxyFactory {
	private static final Logger logger = Logger.getLogger(VOMSProxyFactory.class);
	
    public static final int CERTIFICATE_PEM = 0;
    public static final int CERTIFICATE_PKCS12 = 1;
    public static final String DEFAULTLIFE_TIME = "PT12H";
    private JSAGAVOMSACProxy m_jsagaVomsACProxy;
    private DefaultVOMSACRequest m_vomsACRequest;
    private X509Credential m_userCredential;
    private final String m_userProxyFile;
    private final String vomsdir;
    private final String cadir;

    /**
     * constructor for creating VOMS proxies
     */
    public VOMSProxyFactory(Map attributes, int certificateFormat) throws BadParameterException, ParseException, URISyntaxException {
        this(attributes, certificateFormat, null);
    }

    /**
     * constructor for signing VOMS proxies
     */
    public VOMSProxyFactory(Map attributes, GSSCredential cred) throws BadParameterException, ParseException, URISyntaxException {
        this(attributes, CERTIFICATE_PEM, cred);
    }

    private VOMSProxyFactory(Map attributes, int certificateFormat, GSSCredential cred) throws BadParameterException, ParseException, URISyntaxException {
        // required attributes
        cadir = (String) attributes.get(Context.CERTREPOSITORY);
        vomsdir = (String) attributes.get(VOMSContext.VOMSDIR);

        String serverUrl = (String) attributes.get(Context.SERVER);
        URI uri = new URI(serverUrl.replaceAll(" ", "%20"));
        if (uri.getHost() == null) {
            throw new BadParameterException("Attribute Server has no host name: " + uri.toString());
        }
        DefaultVOMSServerInfo server = new DefaultVOMSServerInfo();
        server.setURL(uri);
        server.setVOMSServerDN(uri.getPath());
        server.setVoName((String) attributes.get(Context.USERVO));
        
        
        
        VOMSRequestListener vomsRequestListener = new VOMSRequestListener() {
			
			public void notifyWarningsInVOMSResponse(VOMSACRequest request, VOMSServerInfo si, VOMSWarningMessage[] warnings) {
				logger.warn("Warnings In VOMS Response : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si + "\n\t- warnings: " + Arrays.toString(warnings));
			}
			
			public void notifyVOMSRequestSuccess(VOMSACRequest request, VOMSServerInfo endpoint) {
				logger.info("VOMS Request Success : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- endpoint: " + endpoint);
			}
			
			public void notifyVOMSRequestStart(VOMSACRequest request, VOMSServerInfo si) {
				logger.info("VOMS Request Start : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si);
			}
			
			public void notifyVOMSRequestFailure(VOMSACRequest request, VOMSServerInfo endpoint, Throwable error) {
				logger.error("Errors In VOMS Reponse : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- endpoint: " + endpoint + "\n\t- errors: " + error.getMessage());
			}
			
			public void notifyErrorsInVOMSReponse(VOMSACRequest request, VOMSServerInfo si, VOMSErrorMessage[] errors) {
				logger.error("Errors In VOMS Reponse : \n\t- req:" + Arrays.toString(request.getRequestedFQANs().toArray()) + "\n\t- si: " + si + "\n\t- errors: " + Arrays.toString(errors));
			}
		};
		
        m_jsagaVomsACProxy = new JSAGAVOMSACProxy(cadir, vomsRequestListener);
        
        if (cred != null) {
            if (cred instanceof GlobusGSSCredentialImpl) {
            	m_userCredential = ((GlobusGSSCredentialImpl) cred).getX509Credential();
            } else {
                throw new BadParameterException("Not a globus proxy");
            }
        } else {
            // get passphrase
            String passphrase = (String) attributes.get(Context.USERPASS);
            if ("".equals(passphrase)) {
                passphrase = null;
            }

            // get certificate
            switch (certificateFormat) {
                case CERTIFICATE_PEM:
                    String userCert = (String) attributes.get(Context.USERCERT);
                    String userKey = (String) attributes.get(Context.USERKEY);
					try {
						PEMCredential pemCredential = new PEMCredential(userKey, userCert, passphrase != null ? passphrase.toCharArray() : null);
						m_userCredential =  new X509Credential(pemCredential.getKey(), pemCredential.getCertificateChain());
					} catch (Exception e) {
						throw new BadParameterException("Unable to load the provided pems files (cert: '" + userCert + "', key: '" + userKey, e);
					}
                    break;
                case CERTIFICATE_PKCS12:
                    String pkcs12 = (String) attributes.get(VOMSContext.USERCERTKEY);
					try {
						KeystoreCredential keystoreCredential = new KeystoreCredential(pkcs12, passphrase != null ? passphrase.toCharArray() : null, null, null, "PKCS12");
						m_userCredential =  new X509Credential(keystoreCredential.getKey(), keystoreCredential.getCertificateChain());
					} catch (Exception e) {
						throw new BadParameterException("Unable to load the provided pkcs12 file (" + pkcs12 + ")");
					}
                    break;
                default:
                    throw new BadParameterException("Invalid case, either PEM or PKCS12 certificates is supported");
            }
        }
        m_jsagaVomsACProxy.addVOMSServerInfo(server);
        m_userProxyFile = (String) attributes.get(Context.USERPROXY);
        m_vomsACRequest = new DefaultVOMSACRequest();
        m_vomsACRequest.setVoName((String) attributes.get(Context.USERVO));

        // optional attributes
        if (attributes.containsKey(VOMSContext.USERFQAN)) {
        	List<String> fqans = new ArrayList<String>();
        	fqans.add((String) attributes.get(VOMSContext.USERFQAN));
        	m_vomsACRequest.setRequestedFQANs(fqans);
        }

        int lifetime;
        if (attributes.containsKey(Context.LIFETIME)) {
            lifetime = UDuration.toInt(attributes.get(Context.LIFETIME));
        } else {
            lifetime = UDuration.toInt(DEFAULTLIFE_TIME);
        }

        m_jsagaVomsACProxy.setProxyLifetime(lifetime);
        m_vomsACRequest.setLifetime(lifetime);
        GSIConstants.DelegationType delegationType = GSIConstants.DelegationType.NONE;

        if (attributes.containsKey(VOMSContext.DELEGATION)) {
            String delegation = (String) attributes.get(VOMSContext.DELEGATION);
            if (delegation.equalsIgnoreCase("none")) {
                delegationType = GSIConstants.DelegationType.NONE;
            } else if (delegation.equalsIgnoreCase("limited")) {
                delegationType = GSIConstants.DelegationType.LIMITED;
            } else if (delegation.equalsIgnoreCase("full")) {
                delegationType = GSIConstants.DelegationType.FULL;
            }
        }

        if (attributes.containsKey(VOMSContext.PROXYTYPE)) {
            String proxyType = (String) attributes.get(VOMSContext.PROXYTYPE);

            if (proxyType.equalsIgnoreCase("old")) {
                switch (delegationType) {
                    case LIMITED:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.LEGACY);
                    	m_jsagaVomsACProxy.setProxyLimited(true);
                        break;
                    case FULL:
                    case NONE:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.LEGACY);
                    	m_jsagaVomsACProxy.setProxyLimited(false);
                        break;
                }
            } else if (proxyType.equalsIgnoreCase("globus")) {
                switch (delegationType) {
                    case LIMITED:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.DRAFT_RFC);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.LIMITED_PROXY_OID));
                        break;
                    case FULL:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.DRAFT_RFC);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.INHERITALL_POLICY_OID));
                        break;
                    case NONE:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.DRAFT_RFC);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.INDEPENDENT_POLICY_OID));
                        break;
                }
            } else if (proxyType.equalsIgnoreCase("RFC3820")) {
                switch (delegationType) {
                    case LIMITED:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.RFC3820);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.LIMITED_PROXY_OID));
                        break;
                    case FULL:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.RFC3820);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.INHERITALL_POLICY_OID));
                        break;
                    case NONE:
                    	m_jsagaVomsACProxy.setProxyType(ProxyType.RFC3820);
                    	m_jsagaVomsACProxy.setProxyLimited(true);
                    	m_jsagaVomsACProxy.setProxyPolicy(new ProxyPolicy(ProxyPolicy.INDEPENDENT_POLICY_OID));
                        break;
                }
            }
        }
    }

    public GSSCredential createProxy() throws GSSException, BadParameterException, NoSuccessException, VOMSException {
        // create
        X509Credential globusProxy;
        if ("NOVO".equals(m_vomsACRequest.getVoName())) {
            // TEST to create gridProxy :
            try {
				globusProxy = m_jsagaVomsACProxy.getVOMSProxyCertificate(m_userCredential, null);
			} catch (CredentialException e) {
				throw new NoSuccessException("Unable to generate the requested Grid proxy (NOVO)", e);
			}
        } else {
            try {
				globusProxy = m_jsagaVomsACProxy.getVOMSProxyCertificate(m_userCredential, m_vomsACRequest);
			} catch (CredentialException e) {
				throw new NoSuccessException("Unable to generate the requested VOMS proxy", e);
			}
            // validate
        	List<String> vomsdirs = new ArrayList<String>();
        	vomsdirs.add(vomsdir);
            List<VOMSAttribute> v = VOMSValidators.newValidator(new DefaultVOMSTrustStore(vomsdirs), CertificateValidatorBuilder.buildCertificateValidator(cadir)).parse(globusProxy.getCertificateChain());
            for (int i = 0; i < v.size(); i++) {
                VOMSAttribute attr = (VOMSAttribute) v.get(i);
                if (!attr.getVO().equals(m_vomsACRequest.getVoName())) {
                    throw new NoSuccessException("The VO name of the created VOMS proxy ('" + attr.getVO() + "') does not match with the required VO name ('" + m_vomsACRequest.getVoName() + "').");
                }
            }
            try {
	            FileOutputStream fileOutputStream = new FileOutputStream(m_userProxyFile);
	            try{
		            globusProxy.save(fileOutputStream);
	            	Util.setFilePermissions(m_userProxyFile, 600);
	            }finally{
	            	try {
						fileOutputStream.close();
					} catch (IOException e) {}
	            }
            } catch (Exception e) {
				throw new NoSuccessException("Unable to save the generated VOMS proxy in '" +m_userProxyFile + "'", e);
			}
        }
        return new GlobusGSSCredentialImpl(globusProxy, GSSCredential.INITIATE_AND_ACCEPT);
    }
}
