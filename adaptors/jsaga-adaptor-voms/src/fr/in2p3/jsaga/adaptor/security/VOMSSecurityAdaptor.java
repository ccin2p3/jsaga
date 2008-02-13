package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.glite.security.voms.VOMSAttribute;
import org.glite.security.voms.VOMSValidator;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

import java.io.PrintStream;
import java.text.ParseException;
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

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, NoSuccess {
        // same as globus
        GlobusCredential globusProxy;
        if (m_proxy instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)m_proxy).getGlobusCredential();
        } else {
            throw new NoSuccess("Not a globus proxy");
        }
        Vector v = VOMSValidator.parse(globusProxy.getCertificateChain());
        VOMSAttribute attr = (VOMSAttribute) v.elementAt(0);
        // get attribute
        if (Context.USERVO.equals(key)) {
            return attr.getVO();
        } else if (VOMSContext.USERFQAN.equals(key)) {
            String value = "";
            for (Iterator it=attr.getFullyQualifiedAttributes().iterator(); it.hasNext(); ) {
                value += it.next()+"\n";
            }
            return value;
        } else if (Context.LIFETIME.equals(key)) {
            try {
                long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
                return ""+(timeleft>0 ? timeleft : 0);
            } catch (ParseException e) {
                throw new NoSuccess(e);
            }
        } else {
            return super.getAttribute(key);
        }
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
