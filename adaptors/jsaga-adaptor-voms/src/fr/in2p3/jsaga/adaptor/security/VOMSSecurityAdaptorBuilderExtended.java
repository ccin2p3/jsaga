package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.glite.security.voms.contact.*;
import org.globus.util.Util;
import org.ogf.saga.error.BadParameter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

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
public class VOMSSecurityAdaptorBuilderExtended extends VOMSSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"), new UFile("CertDir"),
            new U("Server"), new U("UserVO"), new UOptional("UserFQAN"),
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
            new UOptional("Delegation") {
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
            new UOptional("ProxyType") {
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
        System.setProperty("X509_USER_CERT", (String) attributes.get("UserCert"));
        System.setProperty("X509_USER_KEY", (String) attributes.get("UserKey"));
        System.setProperty("X509_CERT_DIR", (String) attributes.get("CertDir"));
        System.setProperty("VOMSDIR", (String) attributes.get("VomsDir"));
        URI uri = new URI((String) attributes.get("Server"));
        if (uri.getHost()==null) {
            throw new BadParameter("Attribute Server has no host name: "+uri.toString());
        }
        VOMSServerInfo server = new VOMSServerInfo();
        server.setHostName(uri.getHost());
        server.setPort(uri.getPort());
        server.setHostDn(uri.getPath());
        server.setVoName((String) attributes.get("UserVO"));
        VOMSProxyInit proxyInit = !"".equals(attributes.get("UserPass"))
                ? VOMSProxyInit.instance((String) attributes.get("UserPass"))
                : VOMSProxyInit.instance();
        proxyInit.addVomsServer(server);
        proxyInit.setProxyOutputFile((String) attributes.get("UserProxy"));
        VOMSRequestOptions o = new VOMSRequestOptions();
        o.setVoName((String) attributes.get("UserVO"));

        // optional attributes
        if (attributes.containsKey("UserFQAN")) {
            o.addFQAN((String) attributes.get("UserFQAN"));
        }
        if (attributes.containsKey("LifeTime")) {
            int lifetime = UDuration.toInt(attributes.get("LifeTime"));
            proxyInit.setProxyLifetime(lifetime);
        }
        if (attributes.containsKey("Delegation")) {
            if (((String)attributes.get("Delegation")).equalsIgnoreCase("none")) {
                proxyInit.setDelegationType(DELEGATION_NONE);
            } else if (((String)attributes.get("Delegation")).equalsIgnoreCase("limited")) {
                proxyInit.setDelegationType(DELEGATION_LIMITED);
            } else if (((String)attributes.get("Delegation")).equalsIgnoreCase("full")) {
                proxyInit.setDelegationType(DELEGATION_FULL);
            }
        }
        if (attributes.containsKey("ProxyType")) {
            if (((String)attributes.get("ProxyType")).equalsIgnoreCase("old")) {
                proxyInit.setProxyType(OID_OLD);
            } else if (((String)attributes.get("ProxyType")).equalsIgnoreCase("globus")) {
                proxyInit.setProxyType(OID_GLOBUS);
            } else if (((String)attributes.get("ProxyType")).equalsIgnoreCase("RFC820")) {
                proxyInit.setProxyType(OID_RFC820);
            }
        }

        // create
        ArrayList options = new ArrayList();
        options.add(o);
        proxyInit.getVomsProxy(options);
    }

    public void destroyBuilder(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get("UserProxy");
        Util.destroy(proxyFile);
    }
}
