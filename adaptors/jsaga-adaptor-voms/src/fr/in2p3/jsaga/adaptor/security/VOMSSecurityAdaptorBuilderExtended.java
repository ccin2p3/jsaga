package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;

import org.glite.security.voms.VOMSAttribute;
import org.glite.security.voms.VOMSValidator;
import org.glite.security.voms.contact.*;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.util.Util;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSSecurityAdaptorBuilderExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSSecurityAdaptorBuilderExtended extends VOMSSecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
    private static final String USERFQAN = "UserFQAN";
    private static final String DELEGATION = "Delegation";
    private static final String PROXYTYPE = "ProxyType";
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new UOr(new Usage[]{
                    new UAnd(new Usage[]{new UFile(Context.USERCERT), new UFile(Context.USERKEY)}),
                    new UFile(USERCERTKEY)
            }),
            new U(Context.USERPROXY), new UHidden(Context.USERPASS), new UFile(Context.CERTREPOSITORY),
            new U(Context.SERVER), new U(Context.USERVO), new UOptional(USERFQAN),
            new UDuration(Context.LIFETIME) {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
            new UOptional(DELEGATION) {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("none") && !v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                            throw new BadParameter("Expected: none | limited | full");
                        }
                    }
                    return value;
                }
            },
            new UOptional(PROXYTYPE) {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC820")) {
                            throw new BadParameter("Expected: old | globus | RFC820");
                        }
                    }
                    return value;
                }
            }
    });

    public Usage getInitUsage() {
        return CREATE_PROXY;
    }

    private static final int OID_OLD = 2;           // default
    private static final int OID_GLOBUS = 3;
    private static final int OID_RFC820 = 4;
    private static final int DELEGATION_NONE = 1;
    private static final int DELEGATION_LIMITED = 2;
    private static final int DELEGATION_FULL = 3;   // default
    
    public void initBuilder(Map attributes, String contextId) throws Exception {
        // required attributes
        System.setProperty("X509_CERT_DIR", (String) attributes.get(Context.CERTREPOSITORY));
        System.setProperty("CADIR", (String) attributes.get(Context.CERTREPOSITORY));
        System.setProperty("VOMSDIR", (String) attributes.get(VOMSDIR));
        
        // optional attributes
        if(attributes.get(Context.USERCERT) != null && 
        		attributes.get(Context.USERKEY) != null) {
        	System.setProperty("X509_USER_CERT", (String) attributes.get(Context.USERCERT));
            System.setProperty("X509_USER_KEY", (String) attributes.get(Context.USERKEY));
        }
        else if (attributes.get(USERCERTKEY) != null) {
        	System.setProperty("PKCS12_USER_CERT", (String) attributes.get(USERCERTKEY));
        }
        else {
        	throw new BadParameter("Invalid case, either PEM or PKCS12 certificates is supported");
        }
        
        URI uri = new URI((String) attributes.get(Context.SERVER));
        if (uri.getHost()==null) {
            throw new BadParameter("Attribute Server has no host name: "+uri.toString());
        }
        VOMSServerInfo server = new VOMSServerInfo();
        server.setHostName(uri.getHost());
        server.setPort(uri.getPort());
        server.setHostDn(uri.getPath());
        server.setVoName((String) attributes.get(Context.USERVO));
        VOMSProxyInit proxyInit = !"".equals(attributes.get(Context.USERPASS))
                ? VOMSProxyInit.instance((String) attributes.get(Context.USERPASS))
                : VOMSProxyInit.instance();
        proxyInit.addVomsServer(server);
        proxyInit.setProxyOutputFile((String) attributes.get(Context.USERPROXY));
        VOMSRequestOptions o = new VOMSRequestOptions();
        o.setVoName((String) attributes.get(Context.USERVO));

        // optional attributes
        if (attributes.containsKey(USERFQAN)) {
            o.addFQAN((String) attributes.get(USERFQAN));
        }
        if (attributes.containsKey(Context.LIFETIME)) {
        	int lifetime = UDuration.toInt(attributes.get(Context.LIFETIME));
            proxyInit.setProxyLifetime(lifetime);
            o.setLifetime(lifetime);
        }
        if (attributes.containsKey(DELEGATION)) {
            String delegation = (String) attributes.get(DELEGATION);
            if (delegation.equalsIgnoreCase("none")) {
                proxyInit.setDelegationType(DELEGATION_NONE);
            } else if (delegation.equalsIgnoreCase("limited")) {
                proxyInit.setDelegationType(DELEGATION_LIMITED);
            } else if (delegation.equalsIgnoreCase("full")) {
                proxyInit.setDelegationType(DELEGATION_FULL);
            }
        }
        if (attributes.containsKey(PROXYTYPE)) {
            String proxyType = (String) attributes.get(PROXYTYPE);
            if (proxyType.equalsIgnoreCase("old")) {
                proxyInit.setProxyType(OID_OLD);
            } else if (proxyType.equalsIgnoreCase("globus")) {
                proxyInit.setProxyType(OID_GLOBUS);
            } else if (proxyType.equalsIgnoreCase("RFC820")) {
                proxyInit.setProxyType(OID_RFC820);
            }
        }

        // create
        GlobusCredential globusProxy = null;
        ArrayList options = new ArrayList();
        options.add(o);
        if(((String) attributes.get(Context.USERVO)).equals("NOVO")) {
        	// TEST to create gridProxy : 
        	globusProxy = proxyInit.getVomsProxy(null);
        }
        else { 
        	globusProxy = proxyInit.getVomsProxy(options);
	        // validate
	        try {
		        Vector v = VOMSValidator.parse(globusProxy.getCertificateChain());
		        for (int i=0; i<v.size(); i++) {
		            VOMSAttribute attr = (VOMSAttribute) v.elementAt(i);
		            if(!attr.getVO().equals((String) attributes.get(Context.USERVO)))
		            	throw new NoSuccess("The VO name of the created VOMS proxy ('"+attr.getVO()+"') does not match with the required VO name ('"+ (String) attributes.get(Context.USERVO)+"').");
		        }
	        }
	        catch (IllegalArgumentException iAE) {
	        	throw new BadParameter("The lifetime may be too long : "+iAE.getMessage());
	        }
        }
    }

    public void destroyBuilder(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
    }
}
