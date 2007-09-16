package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.util.Util;
import org.ogf.saga.error.NotImplemented;

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
public class VOMSSecurityAdaptor implements SecurityAdaptor {
    private GlobusCredential m_globusProxy;

    public VOMSSecurityAdaptor(GlobusCredential globusProxy) {
        m_globusProxy = globusProxy;
    }

    public GlobusCredential getGlobusCredential() {
        return m_globusProxy;
    }

    public void close() throws Exception {
    }

    public void dump(PrintStream out) throws Exception {
        // same as globus
        out.println("  subject  : "+m_globusProxy.getCertificateChain()[0].getSubjectDN());
        out.println("  issuer   : "+m_globusProxy.getCertificateChain()[0].getIssuerDN());
        out.println("  identity : "+m_globusProxy.getIdentity());
        out.println("  type     : "+ CertUtil.getProxyTypeAsString(m_globusProxy.getProxyType()));
        out.println("  strength : "+m_globusProxy.getStrength()+" bits");
        out.println("  timeleft : "+ Util.formatTimeSec(m_globusProxy.getTimeLeft()));

        // VOMS specific
        out.println("  === VO extension information ===");
        throw new NotImplemented("not implemented yet...");
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
