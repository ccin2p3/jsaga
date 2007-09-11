package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.UDuration;
import org.globus.common.CoGProperties;
import org.globus.common.Version;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.proxy.ext.ProxyCertInfo;
import org.globus.gsi.proxy.ext.ProxyPolicy;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.BadParameter;

import java.text.ParseException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusProxyFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusProxyFactory extends GlobusProxyFactoryAbstract {
    private static final int OID_OLD = 2;
    private static final int OID_GLOBUS = 3;    // default
    private static final int OID_RFC820 = 4;
    
    private String m_certFile = null;
    private String m_proxyFile = null;
    private String m_keyFile = null;
    private boolean m_verify = true;
    private boolean m_globusStyle = false;

    public GlobusProxyFactory(Map attributes) throws BadParameter, ParseException {
        // required attributes
        super((String) attributes.get("UserPass")); // UserPass is ignored if key is not encrypted
        CoGProperties.getDefault().setCaCertLocations((String) attributes.get("CertDir"));
        m_proxyFile = (String) attributes.get("UserProxy");
        m_certFile = (String) attributes.get("UserCert");
        m_keyFile = (String) attributes.get("UserKey");
        
        // optional attributes
        if (attributes.containsKey("LifeTime")) {
            lifetime = UDuration.toInt(attributes.get("LifeTime"));
        }
        boolean limited = false;
        if (attributes.containsKey("Delegation")) {
            limited = ((String)attributes.get("Delegation")).equalsIgnoreCase("limited");
        }
        int oid = OID_GLOBUS;
        if (attributes.containsKey("ProxyType")) {
            if (((String)attributes.get("ProxyType")).equalsIgnoreCase("old")) {
                oid = OID_OLD;
            } else if (((String)attributes.get("ProxyType")).equalsIgnoreCase("RFC820")) {
                oid = OID_RFC820;
            }
        }
        switch(oid) {
            case OID_OLD:
                proxyType = (limited) ?
                        GSIConstants.GSI_2_LIMITED_PROXY :
                        GSIConstants.GSI_2_PROXY;
                break;
            case OID_GLOBUS:
                proxyType = (limited) ?
                        GSIConstants.GSI_3_LIMITED_PROXY :
                        GSIConstants.GSI_3_IMPERSONATION_PROXY;
                break;
            case OID_RFC820:
                proxyType = (limited) ?
                        GSIConstants.GSI_4_LIMITED_PROXY :
                        GSIConstants.GSI_4_IMPERSONATION_PROXY;
                break;
        }
    }

    public GSSCredential createProxy() throws GSSException {
        CertUtil.init();

        ProxyCertInfo proxyCertInfo = null;
        if ((CertUtil.isGsi3Proxy(proxyType)) || (CertUtil.isGsi4Proxy(proxyType))) {
            ProxyPolicy policy;
            if (CertUtil.isLimitedProxy(proxyType)) {
                policy = new ProxyPolicy(ProxyPolicy.LIMITED);
            } else if (CertUtil.isIndependentProxy(proxyType)) {
                policy = new ProxyPolicy(ProxyPolicy.INDEPENDENT);
            } else if (CertUtil.isImpersonationProxy(proxyType)) {
                policy = new ProxyPolicy(ProxyPolicy.IMPERSONATION);
            } else {
                throw new IllegalArgumentException("Invalid proxyType");
            }
            proxyCertInfo = new ProxyCertInfo(policy);
        }

        super.setBits(bits);
        super.setLifetime(lifetime);
        super.setProxyType(proxyType);
        super.setProxyCertInfo(proxyCertInfo);
        super.setDebug(false);
        super.setQuiet(false);
        super.setStdin(false);

        super.createProxy(m_certFile, m_keyFile, m_verify, m_globusStyle, m_proxyFile);
        return new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_ONLY);
    }
    
    public String getVersion() {
        return Version.getVersion();
    }
}
