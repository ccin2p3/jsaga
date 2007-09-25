package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
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
 * File:   VOMSSecurityAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   11 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class VOMSSecurityAdaptor extends GSSCredentialSecurityAdaptor implements SecurityAdaptor {
    public VOMSSecurityAdaptor(GSSCredential proxy) {
        super(proxy);
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        // same as globus
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

        // VOMS specific
        out.println("  === VO extension information ===");
        out.println("  Not implemented yet...");
/*
        out.println("  VO        : dteam");
        out.println("  subject   : /O=GRID-FR/C=FR/O=CNRS/OU=CC-LYON/CN=Sylvain Reynaud");
        out.println("  issuer    : /DC=ch/DC=cern/OU=computers/CN=voms.cern.ch");
        out.println("  attribute : /dteam/Role=NULL/Capability=NULL");
        out.println("  attribute : /dteam/france/Role=NULL/Capability=NULL");
        out.println("  attribute : /dteam/france/IN2P3-CC/Role=NULL/Capability=NULL");
        out.println("  timeleft  : 11:59:37");
*/
    }
}
