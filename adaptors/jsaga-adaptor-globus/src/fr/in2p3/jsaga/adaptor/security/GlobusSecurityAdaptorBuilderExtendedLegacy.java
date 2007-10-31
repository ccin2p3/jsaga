package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorBuilderExtendedLegacy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorBuilderExtendedLegacy extends GlobusSecurityAdaptorBuilderExtendedAbstract {
    public String getType() {
        return "GlobusLegacy";
    }

    public boolean checkType(GSSCredential proxy) {
        if (proxy instanceof GlobusGSSCredentialImpl) {
            GlobusCredential globusProxy = ((GlobusGSSCredentialImpl)proxy).getGlobusCredential();
            return CertUtil.isGsi2Proxy(globusProxy.getProxyType());
        } else {
            return false;
        }
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        new GlobusProxyFactory(attributes, GlobusProxyFactory.OID_OLD).createProxy();
    }
}
