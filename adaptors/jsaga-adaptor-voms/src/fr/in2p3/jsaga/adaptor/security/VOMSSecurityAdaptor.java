package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.glite.security.voms.VOMSAttribute;
import org.glite.security.voms.VOMSValidator;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

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
        Vector v = VOMSValidator.parse(globusProxy.getCertificateChain());
        for (int i=0; i<v.size(); i++) {
            VOMSAttribute attr = (VOMSAttribute) v.elementAt(i);
            out.println("  === VO "+attr.getVO()+" extension information ===");
            out.println("  VO        : "+attr.getVO());
            out.println("  subject   : "+globusProxy.getIdentity());
            out.println("  issuer    : "+attr.getIssuerX509());
            for (Iterator it=attr.getFullyQualifiedAttributes().iterator(); it.hasNext(); ) {
                out.println("  attribute : "+it.next());
            }
            long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
            if(timeleft<0) timeleft=0;
            out.println("  timeleft  : "+Util.formatTimeSec(timeleft));
        }
    }
}
