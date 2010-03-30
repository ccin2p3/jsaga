package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorExtendedLegacy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorExtendedLegacy extends GlobusSecurityAdaptor implements ExpirableSecurityAdaptor {
    public String getType() {
        return "GlobusLegacy";
    }

    protected int getGlobusType() {
        return GlobusProxyFactory.OID_OLD;
    }

    protected boolean checkType(GSSCredential proxy) {
        if (proxy instanceof GlobusGSSCredentialImpl) {
            GlobusCredential globusProxy = ((GlobusGSSCredentialImpl)proxy).getGlobusCredential();
            return CertUtil.isGsi2Proxy(globusProxy.getProxyType());
        } else {
            return false;
        }
    }
}
