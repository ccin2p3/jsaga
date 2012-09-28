package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.util.ProxyCertificateUtil;
import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorExtended extends GlobusSecurityAdaptor implements ExpirableSecurityAdaptor {
    public String getType() {
        return "Globus";
    }

    protected int getGlobusType() {
        return GlobusProxyFactory.OID_GLOBUS;
    }

    protected boolean checkType(GSSCredential proxy) {
        if (proxy instanceof GlobusGSSCredentialImpl) {
            X509Credential globusProxy = ((GlobusGSSCredentialImpl)proxy).getX509Credential();
            return ProxyCertificateUtil.isGsi3Proxy(globusProxy.getProxyType());
        } else {
            return false;
        }
    }
}
