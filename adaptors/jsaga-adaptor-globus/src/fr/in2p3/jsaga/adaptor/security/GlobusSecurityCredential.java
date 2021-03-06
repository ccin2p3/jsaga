package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.io.PrintStream;
import org.globus.gsi.X509Credential;
import org.globus.gsi.util.ProxyCertificateUtil;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityCredential extends GSSCredentialSecurityCredential implements SecurityCredential {
    public GlobusSecurityCredential(GSSCredential proxy, File certRepository) {
        super(proxy, certRepository);
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        X509Credential globusProxy;
        if (m_proxy instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)m_proxy).getX509Credential();
        } else {
            throw new Exception("Not a globus proxy");
        }
        out.println("  subject  : "+globusProxy.getSubject());
        out.println("  issuer   : "+globusProxy.getIssuer());
        out.println("  identity : "+globusProxy.getIdentity());
        out.println("  type     : "+ProxyCertificateUtil.getProxyTypeAsString(globusProxy.getProxyType()));
        out.println("  strength : "+globusProxy.getStrength()+" bits");
        out.println("  timeleft : "+Util.formatTimeSec(globusProxy.getTimeLeft()));
    }
}
