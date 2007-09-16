package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptor implements SecurityAdaptor {
    protected GSSCredential m_proxy;

    public GlobusSecurityAdaptor(GSSCredential proxy) {
        m_proxy = proxy;
    }

    public GSSCredential getGSSCredential() {
        return m_proxy;
    }

    public void close() throws Exception {
        //m_proxy.dispose();
    }

    public void dump(PrintStream out) throws Exception {
        GlobusCredential globusProxy;
        if (m_proxy instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)m_proxy).getGlobusCredential();
        } else {
            throw new Exception("Not a globus proxy");
        }
        out.println("  subject  : "+globusProxy.getCertificateChain()[0].getSubjectDN());
        out.println("  issuer   : "+globusProxy.getCertificateChain()[0].getIssuerDN());
        out.println("  identity : "+globusProxy.getIdentity());
        out.println("  type     : "+CertUtil.getProxyTypeAsString(globusProxy.getProxyType()));
        out.println("  strength : "+globusProxy.getStrength()+" bits");
        out.println("  timeleft : "+Util.formatTimeSec(globusProxy.getTimeLeft()));
    }
}
