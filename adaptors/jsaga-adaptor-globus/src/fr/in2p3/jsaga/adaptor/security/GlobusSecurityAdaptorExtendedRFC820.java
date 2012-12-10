package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.util.ProxyCertificateUtil;
import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorExtendedRFC820
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorExtendedRFC820 extends GlobusSecurityAdaptor implements ExpirableSecurityAdaptor {
    public String getType() {
        return "GlobusRFC820";
    }

    protected int getGlobusType() {
        return GlobusProxyFactory.OID_RFC820;
    }

    protected boolean checkType(GSSCredential proxy) {
        if (proxy instanceof GlobusGSSCredentialImpl) {
            X509Credential globusProxy = ((GlobusGSSCredentialImpl)proxy).getX509Credential();
            return ProxyCertificateUtil.isGsi4Proxy(globusProxy.getProxyType());
        } else {
            return false;
        }        
    }
}
