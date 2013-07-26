package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSValidators;
import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
import org.italiangrid.voms.store.impl.DefaultVOMSTrustStore;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.globus.gsi.X509Credential;
import org.globus.gsi.util.CertificateUtil;
import org.globus.gsi.util.ProxyCertificateUtil;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSSecurityCredential
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   11 aout 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class VOMSSecurityCredential extends GSSCredentialSecurityCredential implements SecurityCredential {

    protected File m_userProxyFilename;
    protected File m_vomsDir;

    public VOMSSecurityCredential(GSSCredential proxy, Map attributes) {
        super(proxy, new File((String) attributes.get(Context.CERTREPOSITORY)));
        m_userProxyFilename = new File((String) attributes.get(Context.USERPROXY));
        m_vomsDir = new File((String) attributes.get(VOMSContext.VOMSDIR));
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        // same as globus
        X509Credential globusProxy;
        if (m_proxy instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl)m_proxy).getX509Credential();
        } else {
            throw new NoSuccessException("Not a globus proxy");
        }
        if (Context.USERPROXY.equals(key)) {
			return this.m_userProxyFilename.getAbsolutePath();
        }
        ArrayList<String> vomsdirs = new ArrayList<String>(1);
        vomsdirs.add(m_vomsDir.getAbsolutePath());
        List<VOMSValidationResult> v = new DefaultVOMSValidator.Builder()
        							.trustStore(new DefaultVOMSTrustStore(vomsdirs))
//        							.certChainValidator(v)
        							.build()
        							.validateWithResult(globusProxy.getCertificateChain());
        
        VOMSAttribute attr = ((VOMSValidationResult) v.get(0)).getAttributes();
        // get attribute
        if (Context.USERVO.equals(key)) {
            return attr.getVO();
        } else if (VOMSContext.USERFQAN.equals(key)) {
            String value = "";
            for (Iterator<String> it=attr.getFQANs().iterator(); it.hasNext(); ) {
                value += it.next()+"\n";
            }
            return value;
        } else if (Context.LIFETIME.equals(key)) {
            long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
            return ""+(timeleft>0 ? timeleft : 0);
        } else {
            return super.getAttribute(key);
        }
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        // same as globus
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

        // VOMS specific
        List<VOMSAttribute> v = VOMSValidators.newParser().parse(globusProxy.getCertificateChain());
        for (int i=0; i<v.size(); i++) {
            VOMSAttribute attr = (VOMSAttribute) v.get(i);
            out.println("  === VO "+attr.getVO()+" extension information ===");
            out.println("  VO        : "+attr.getVO());
            out.println("  subject   : "+globusProxy.getIdentity());
            out.println("  issuer    : "+CertificateUtil.toGlobusID(attr.getIssuer()));
            for (Iterator<String> it=attr.getFQANs().iterator(); it.hasNext(); ) {
                out.println("  attribute : "+it.next());
            }
            long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
            if(timeleft<0) timeleft=0;
            out.println("  timeleft  : "+Util.formatTimeSec(timeleft));
        }
    }
}
