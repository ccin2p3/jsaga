package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.UDuration;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.glite.voms.contact.*;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSProxyFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSProxyFactory {
    public static final int CERTIFICATE_PEM = 0;
    public static final int CERTIFICATE_PKCS12 = 1;
    private static final int OID_OLD = 2;           // default
    private static final int OID_GLOBUS = 3;
    private static final int OID_RFC820 = 4;
    private static final int DELEGATION_NONE = 1;
    private static final int DELEGATION_LIMITED = 2;
    private static final int DELEGATION_FULL = 3;   // default

    private VOMSProxyInit m_proxyInit;
    private VOMSRequestOptions m_requestOptions;

    /** constructor for creating VOMS proxies */
    public VOMSProxyFactory(Map attributes, int certificateFormat) throws BadParameterException, ParseException, URISyntaxException {
        this(attributes, certificateFormat, null);
    }
    /** constructor for signing VOMS proxies */
    public VOMSProxyFactory(Map attributes, GSSCredential cred) throws BadParameterException, ParseException, URISyntaxException {
        this(attributes, CERTIFICATE_PEM, cred);
    }
    private VOMSProxyFactory(Map attributes, int certificateFormat, GSSCredential cred) throws BadParameterException, ParseException, URISyntaxException {
        // required attributes
        System.setProperty("X509_CERT_DIR", (String) attributes.get(Context.CERTREPOSITORY));
        System.setProperty("CADIR", (String) attributes.get(Context.CERTREPOSITORY));
        System.setProperty("VOMSDIR", (String) attributes.get(VOMSContext.VOMSDIR));

        URI uri = new URI((String) attributes.get(Context.SERVER));
        if (uri.getHost()==null) {
            throw new BadParameterException("Attribute Server has no host name: "+uri.toString());
        }
        VOMSServerInfo server = new VOMSServerInfo();
        server.setHostName(uri.getHost());
        server.setPort(uri.getPort());
        server.setHostDn(uri.getPath());
        server.setVoName((String) attributes.get(Context.USERVO));
        if (cred != null) {
            if (cred instanceof GlobusGSSCredentialImpl) {
                m_proxyInit = VOMSProxyInit.instance(((GlobusGSSCredentialImpl)cred).getGlobusCredential());
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
            switch(certificateFormat) {
                case CERTIFICATE_PEM:
                    String userCert = (String) attributes.get(Context.USERCERT);
                    String userKey = (String) attributes.get(Context.USERKEY);
                    m_proxyInit = VOMSProxyInit.instance(userCert, userKey, passphrase);
                    break;
                case CERTIFICATE_PKCS12:
                    String pkcs12 = (String) attributes.get(VOMSContext.USERCERTKEY);
                    m_proxyInit = VOMSProxyInit.instance(new File(pkcs12), passphrase);
                    break;
                default:
                    throw new BadParameterException("Invalid case, either PEM or PKCS12 certificates is supported");
            }
        }
        m_proxyInit.addVomsServer(server);
        m_proxyInit.setProxyOutputFile((String) attributes.get(Context.USERPROXY));
        m_requestOptions = new VOMSRequestOptions();
        m_requestOptions.setVoName((String) attributes.get(Context.USERVO));

        // optional attributes
        if (attributes.containsKey(VOMSContext.USERFQAN)) {
            m_requestOptions.addFQAN((String) attributes.get(VOMSContext.USERFQAN));
        }
        if (attributes.containsKey(Context.LIFETIME)) {
        	int lifetime = UDuration.toInt(attributes.get(Context.LIFETIME));
            m_proxyInit.setProxyLifetime(lifetime);
            m_requestOptions.setLifetime(lifetime);
        }
        if (attributes.containsKey(VOMSContext.DELEGATION)) {
            String delegation = (String) attributes.get(VOMSContext.DELEGATION);
            if (delegation.equalsIgnoreCase("none")) {
                m_proxyInit.setDelegationType(DELEGATION_NONE);
            } else if (delegation.equalsIgnoreCase("limited")) {
                m_proxyInit.setDelegationType(DELEGATION_LIMITED);
            } else if (delegation.equalsIgnoreCase("full")) {
                m_proxyInit.setDelegationType(DELEGATION_FULL);
            }
        }
        if (attributes.containsKey(VOMSContext.PROXYTYPE)) {
            String proxyType = (String) attributes.get(VOMSContext.PROXYTYPE);
            if (proxyType.equalsIgnoreCase("old")) {
                m_proxyInit.setProxyType(OID_OLD);
            } else if (proxyType.equalsIgnoreCase("globus")) {
                m_proxyInit.setProxyType(OID_GLOBUS);
            } else if (proxyType.equalsIgnoreCase("RFC820")) {
                m_proxyInit.setProxyType(OID_RFC820);
            }
        }
    }

    public GSSCredential createProxy() throws GSSException, BadParameterException, NoSuccessException {
        // create
        GlobusCredential globusProxy;
        if("NOVO".equals(m_requestOptions.getVoName())) {
        	// TEST to create gridProxy :
        	globusProxy = m_proxyInit.getVomsProxy(null);
        }
        else {
            ArrayList options = new ArrayList();
            options.add(m_requestOptions);
        	globusProxy = m_proxyInit.getVomsProxy(options);
	        // validate
	        try {
		        Vector v = VOMSValidator.parse(globusProxy.getCertificateChain());
		        for (int i=0; i<v.size(); i++) {
		            VOMSAttribute attr = (VOMSAttribute) v.elementAt(i);
		            if(!attr.getVO().equals(m_requestOptions.getVoName()))
		            	throw new NoSuccessException("The VO name of the created VOMS proxy ('"+attr.getVO()+"') does not match with the required VO name ('"+m_requestOptions.getVoName()+"').");
		        }
	        }
	        catch (IllegalArgumentException iAE) {
	        	throw new BadParameterException("The lifetime may be too long", iAE);
	        }
        }
        return new GlobusGSSCredentialImpl(globusProxy, GSSCredential.INITIATE_AND_ACCEPT);
    }
}
